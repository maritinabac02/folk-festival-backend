package com.folkfest.service;

import com.folkfest.dto.PerformanceDtos.*;
import com.folkfest.exception.ApiException;
import com.folkfest.model.*;
import com.folkfest.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
public class PerformanceService {
    private final PerformanceRepository perfRepo;
    private final FestivalRepository festRepo;
    private final UserRepository users;
    private final RoleService roles;

    public PerformanceService(PerformanceRepository perfRepo,
                              FestivalRepository festRepo,
                              UserRepository users,
                              RoleService roles) {
        this.perfRepo = perfRepo; this.festRepo = festRepo; this.users = users; this.roles = roles;
    }

    public Performance create(CreatePerformanceRequest r){
        Festival f = festRepo.findById(r.festivalId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        if (perfRepo.existsByFestivalIdAndNameIgnoreCase(r.festivalId, r.name))
            throw new ApiException(HttpStatus.CONFLICT, "Performance name exists in this festival");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Performance p = new Performance();
        p.setFestivalId(r.festivalId);
        p.setName(r.name);
        p.setDescription(r.description);
        p.setGenre(r.genre);
        p.setDurationMinutes(r.durationMinutes);
        p.setCreatedAt(Instant.now());
        p.setState(PerformanceState.CREATED);
        p.setMainArtist(username);
        p = perfRepo.save(p);

        // Ο δημιουργός γίνεται ARTIST για το festival (αναφορά)
        roles.grantRole(username, r.festivalId, Role.ARTIST);
        return p;
    }

    public Performance update(String id, UpdatePerformanceRequest r){
        Performance p = getExisting(id);
        String username = authUser();
        // μόνο ARTIST του performance μπορεί να το ενημερώσει, και όχι μετά το SUBMITTED (αναφορά)
        ensureArtistOfPerformance(username, p);
        if (p.getState() != PerformanceState.CREATED && p.getState() != PerformanceState.SUBMITTED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Updates allowed only before final submission flow");

        if (r.name != null) p.setName(r.name);
        if (r.description != null) p.setDescription(r.description);
        if (r.genre != null) p.setGenre(r.genre);
        if (r.durationMinutes != null) p.setDurationMinutes(r.durationMinutes);
        if (r.bandMembers != null) p.setBandMembers(r.bandMembers);
        if (r.technicalRequirements != null) p.setTechnicalRequirements(r.technicalRequirements);
        if (r.setlist != null) p.setSetlist(r.setlist);
        if (r.preferredRehearsalTimes != null) p.setPreferredRehearsalTimes(r.preferredRehearsalTimes);
        if (r.preferredPerformanceSlots != null) p.setPreferredPerformanceSlots(r.preferredPerformanceSlots);
        return perfRepo.save(p);
    }

    public void withdraw(String id){
        Performance p = getExisting(id);
        String username = authUser();
        ensureArtistOfPerformance(username, p);
        if (p.getState() != PerformanceState.CREATED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Withdraw only before SUBMITTED");
        perfRepo.deleteById(id);
    }

    public Performance submit(String id){
        Performance p = getExisting(id);
        String username = authUser();
        ensureArtistOfPerformance(username, p);

        Festival f = festRepo.findById(p.getFestivalId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        if (f.getState() != FestivalState.SUBMISSION)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Festival not in SUBMISSION");

        // completeness checks (name, description, genre, duration, bandMembers, techReq, setlist, rehearsal, performance)
        if (isEmpty(p.getName()) || isEmpty(p.getDescription()) || isEmpty(p.getGenre())
                || p.getDurationMinutes() == null || empty(p.getBandMembers())
                || empty(p.getTechnicalRequirements()) || empty(p.getSetlist())
                || empty(p.getPreferredRehearsalTimes()) || empty(p.getPreferredPerformanceSlots()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Incomplete performance details");

        p.setState(PerformanceState.SUBMITTED);
        return perfRepo.save(p);
    }

    public Performance assignStaff(String id, String staffUsername){
        Performance p = getExisting(id);
        String username = authUser();
        // ORGANIZER εκτελεί assignment (αναφορά)
        roles.ensureRole(username, p.getFestivalId(), Role.ORGANIZER);

        Festival f = festRepo.findById(p.getFestivalId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        if (f.getState() != FestivalState.ASSIGNMENT)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Festival not in ASSIGNMENT");
        // Το προσωπικό πρέπει να είναι STAFF στο festival
        roles.ensureRole(staffUsername, p.getFestivalId(), Role.STAFF);

        p.setAssignedStaff(staffUsername);
        return perfRepo.save(p);
    }

    public Performance review(String id, ReviewRequest r){
        Performance p = getExisting(id);
        String username = authUser();

        Festival f = festRepo.findById(p.getFestivalId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        if (f.getState() != FestivalState.REVIEW)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Festival not in REVIEW");

        // μόνο ο assigned STAFF κάνει review
        if (!Objects.equals(username, p.getAssignedStaff()))
            throw new ApiException(HttpStatus.FORBIDDEN, "Only assigned staff can review");

        p.setReviewScore(r.score);
        p.setReviewComments(r.comments);
        p.setState(PerformanceState.REVIEWED);
        return perfRepo.save(p);
    }

    public Performance approve(String id){
        Performance p = getExisting(id);
        String username = authUser();
        roles.ensureRole(username, p.getFestivalId(), Role.ORGANIZER);

        Festival f = festRepo.findById(p.getFestivalId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        if (f.getState() != FestivalState.SCHEDULING)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Festival not in SCHEDULING");

        if (p.getState() != PerformanceState.REVIEWED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Approve allowed only after review");
        p.setState(PerformanceState.APPROVED);
        return perfRepo.save(p);
    }

    public Performance reject(String id, String reason){
        Performance p = getExisting(id);
        String username = authUser();
        roles.ensureRole(username, p.getFestivalId(), Role.ORGANIZER);

        Festival f = festRepo.findById(p.getFestivalId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        if (f.getState() != FestivalState.SCHEDULING && f.getState() != FestivalState.DECISION)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reject allowed in SCHEDULING or DECISION");

        p.setRejectionReason(reason != null ? reason : "Rejected");
        p.setState(PerformanceState.REJECTED);
        return perfRepo.save(p);
    }

    public Performance finalSubmit(String id, FinalSubmissionRequest r){
        Performance p = getExisting(id);
        String username = authUser();
        ensureArtistOfPerformance(username, p);

        Festival f = festRepo.findById(p.getFestivalId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        if (f.getState() != FestivalState.FINAL_SUBMISSION)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Festival not in FINAL_SUBMISSION");

        if (p.getState() != PerformanceState.APPROVED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only approved performances can final submit");

        if (empty(r.setlist) || empty(r.rehearsalTimes) || empty(r.performanceSlots))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Final submission requires setlist/rehearsal/performance slots");

        p.setSetlist(r.setlist);
        p.setPreferredRehearsalTimes(r.rehearsalTimes);
        p.setPreferredPerformanceSlots(r.performanceSlots);
        // δεν αλλάζουμε state εδώ — το state αλλάζει σε DECISION φάση με accept/reject
        return perfRepo.save(p);
    }

    public Performance accept(String id, String scheduledTime, String scheduledStage){
        Performance p = getExisting(id);
        String username = authUser();
        roles.ensureRole(username, p.getFestivalId(), Role.ORGANIZER);

        Festival f = festRepo.findById(p.getFestivalId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        if (f.getState() != FestivalState.DECISION)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Festival not in DECISION");

        if (isEmpty(scheduledTime) || isEmpty(scheduledStage))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Accept requires scheduledTime & scheduledStage");

        p.setScheduledTime(scheduledTime);
        p.setScheduledStage(scheduledStage);
        p.setState(PerformanceState.SCHEDULED);
        return perfRepo.save(p);
    }

    public Performance view(String id){ return getExisting(id); }

    // Helpers
    private Performance getExisting(String id){
        return perfRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Performance not found"));
    }
    private void ensureArtistOfPerformance(String username, Performance p){
        if (!username.equals(p.getMainArtist()) && (p.getBandMembers()==null || !p.getBandMembers().contains(username)))
            throw new ApiException(HttpStatus.FORBIDDEN, "Only main artist or band member can modify");
    }
    private String authUser(){ return SecurityContextHolder.getContext().getAuthentication().getName(); }
    private boolean isEmpty(String s){ return s==null || s.isBlank(); }
    private boolean empty(java.util.Collection<?> c){ return c==null || c.isEmpty(); }
}
