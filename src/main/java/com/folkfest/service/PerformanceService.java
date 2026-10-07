package com.folkfest.service;

import com.folkfest.dto.PerformanceDtos.*;
import com.folkfest.exception.ApiException;
import com.folkfest.model.*;
import com.folkfest.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

@Service
public class PerformanceService {
    private final PerformanceRepository perfRepo;
    private final FestivalRepository festRepo;
    private final RoleService roles;

    public PerformanceService(PerformanceRepository perfRepo,
                              FestivalRepository festRepo,
                              RoleService roles) {
        this.perfRepo = perfRepo; this.festRepo = festRepo; this.roles = roles;
    }

    public Performance create(CreatePerformanceRequest r) {
        getFestival(r.festivalId);
        if (perfRepo.existsByFestivalIdAndNameIgnoreCase(r.festivalId, r.name))
            throw new ApiException(HttpStatus.CONFLICT, "Performance name exists in this festival");

        String username = authUser();
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

        // the creator becomes an ARTIST of this festival
        roles.grantRole(username, r.festivalId, Role.ARTIST);
        return p;
    }

    public Performance update(String id, UpdatePerformanceRequest r) {
        Performance p = getExisting(id);
        ensureArtistOfPerformance(authUser(), p);
        if (p.getState() != PerformanceState.CREATED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Performance can only be edited before submission");

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

    public void withdraw(String id) {
        Performance p = getExisting(id);
        ensureArtistOfPerformance(authUser(), p);
        if (p.getState() != PerformanceState.CREATED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Withdraw only before SUBMITTED");
        perfRepo.deleteById(id);
    }

    public Performance submit(String id) {
        Performance p = getExisting(id);
        ensureArtistOfPerformance(authUser(), p);
        ensureFestivalState(p, FestivalState.SUBMISSION);

        if (isEmpty(p.getName()) || isEmpty(p.getDescription()) || isEmpty(p.getGenre())
                || p.getDurationMinutes() == null || isEmpty(p.getBandMembers())
                || isEmpty(p.getTechnicalRequirements()) || isEmpty(p.getSetlist())
                || isEmpty(p.getPreferredRehearsalTimes()) || isEmpty(p.getPreferredPerformanceSlots()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Incomplete performance details");

        p.setState(PerformanceState.SUBMITTED);
        return perfRepo.save(p);
    }

    public Performance assignStaff(String id, String staffUsername) {
        Performance p = getExisting(id);
        roles.ensureRole(authUser(), p.getFestivalId(), Role.ORGANIZER);
        ensureFestivalState(p, FestivalState.ASSIGNMENT);
        roles.ensureRole(staffUsername, p.getFestivalId(), Role.STAFF);

        p.setAssignedStaff(staffUsername);
        return perfRepo.save(p);
    }

    public Performance review(String id, ReviewRequest r) {
        Performance p = getExisting(id);
        ensureFestivalState(p, FestivalState.REVIEW);

        if (!Objects.equals(authUser(), p.getAssignedStaff()))
            throw new ApiException(HttpStatus.FORBIDDEN, "Only assigned staff can review");
        if (p.getState() != PerformanceState.SUBMITTED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only submitted performances can be reviewed");

        p.setReview(new Review(r.score, r.comments));
        p.setState(PerformanceState.REVIEWED);
        return perfRepo.save(p);
    }

    public Performance approve(String id) {
        Performance p = getExisting(id);
        roles.ensureRole(authUser(), p.getFestivalId(), Role.ORGANIZER);
        ensureFestivalState(p, FestivalState.SCHEDULING);

        if (p.getState() != PerformanceState.REVIEWED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Approve allowed only after review");
        p.setState(PerformanceState.APPROVED);
        return perfRepo.save(p);
    }

    public Performance reject(String id, String reason) {
        Performance p = getExisting(id);
        roles.ensureRole(authUser(), p.getFestivalId(), Role.ORGANIZER);

        FestivalState fs = getFestival(p.getFestivalId()).getState();
        if (fs != FestivalState.SCHEDULING && fs != FestivalState.DECISION)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reject allowed in SCHEDULING or DECISION");
        if (p.getState() == PerformanceState.SCHEDULED || p.getState() == PerformanceState.REJECTED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Performance is already " + p.getState());

        p.setRejectionReason(isEmpty(reason) ? "Rejected" : reason);
        p.setState(PerformanceState.REJECTED);
        return perfRepo.save(p);
    }

    public Performance finalSubmit(String id, FinalSubmissionRequest r) {
        Performance p = getExisting(id);
        ensureArtistOfPerformance(authUser(), p);
        ensureFestivalState(p, FestivalState.FINAL_SUBMISSION);

        if (p.getState() != PerformanceState.APPROVED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only approved performances can final submit");
        if (isEmpty(r.setlist) || isEmpty(r.rehearsalTimes) || isEmpty(r.performanceSlots))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Final submission requires setlist/rehearsal/performance slots");

        p.setSetlist(r.setlist);
        p.setPreferredRehearsalTimes(r.rehearsalTimes);
        p.setPreferredPerformanceSlots(r.performanceSlots);
        // state stays APPROVED; the organizer accepts or rejects in the DECISION phase
        return perfRepo.save(p);
    }

    public Performance accept(String id, String scheduledTime, String scheduledStage) {
        Performance p = getExisting(id);
        roles.ensureRole(authUser(), p.getFestivalId(), Role.ORGANIZER);
        ensureFestivalState(p, FestivalState.DECISION);

        if (p.getState() != PerformanceState.APPROVED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only approved performances can be scheduled");
        if (isEmpty(scheduledTime) || isEmpty(scheduledStage))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Accept requires scheduledTime & scheduledStage");

        p.setScheduledTime(scheduledTime);
        p.setScheduledStage(scheduledStage);
        p.setState(PerformanceState.SCHEDULED);
        return perfRepo.save(p);
    }

    /** Users with no role in the festival only see scheduled performances, without private details. */
    public Performance view(String id) {
        Performance p = getExisting(id);
        Role role = roles.getUserRoleForFestival(authUser(), p.getFestivalId());
        if (role == null) {
            if (p.getState() != PerformanceState.SCHEDULED)
                throw new ApiException(HttpStatus.NOT_FOUND, "Performance not found");
            p.stripSensitiveForVisitor();
        }
        return p;
    }

    private Performance getExisting(String id) {
        return perfRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Performance not found"));
    }

    private Festival getFestival(String festivalId) {
        return festRepo.findById(festivalId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
    }

    private void ensureFestivalState(Performance p, FestivalState expected) {
        if (getFestival(p.getFestivalId()).getState() != expected)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Festival not in " + expected);
    }

    private void ensureArtistOfPerformance(String username, Performance p) {
        boolean isMain = username.equals(p.getMainArtist());
        boolean isMember = p.getBandMembers() != null && p.getBandMembers().contains(username);
        if (!isMain && !isMember)
            throw new ApiException(HttpStatus.FORBIDDEN, "Only main artist or band member can modify");
    }

    private String authUser() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
    private boolean isEmpty(String s) { return s == null || s.isBlank(); }
    private boolean isEmpty(Collection<?> c) { return c == null || c.isEmpty(); }
}