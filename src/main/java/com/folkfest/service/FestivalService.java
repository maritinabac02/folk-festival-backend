package com.folkfest.service;

import com.folkfest.dto.FestivalDtos.*;
import com.folkfest.exception.ApiException;
import com.folkfest.model.*;
import com.folkfest.repo.FestivalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class FestivalService {
    private final FestivalRepository festivals;
    private final RoleService roles;

    public FestivalService(FestivalRepository festivals, RoleService roles) {
        this.festivals = festivals; this.roles = roles;
    }

    public Festival create(CreateFestivalRequest r){
        if (festivals.existsByName(r.name))
            throw new ApiException(HttpStatus.CONFLICT, "Festival name already exists");

        var username = SecurityContextHolder.getContext().getAuthentication().getName();

        Festival f = new Festival();
        f.setName(r.name);
        f.setDescription(r.description);
        f.setVenue(r.venue);
        f.setStartDate(r.startDate);
        f.setEndDate(r.endDate);
        f.setState(FestivalState.CREATED);
        f = festivals.save(f);

        roles.grantRole(username, f.getId(), Role.ORGANIZER);
        return f;
    }

    public Festival update(String id, UpdateFestivalRequest r){
        Festival f = festivals.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        roles.ensureRole(username, id, Role.ORGANIZER);

        if (r.name != null) f.setName(r.name);
        if (r.description != null) f.setDescription(r.description);
        if (r.venue != null) f.setVenue(r.venue);
        return festivals.save(f);
    }

    public Festival changeState(String id, FestivalState next){
        Festival f = festivals.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        roles.ensureRole(username, id, Role.ORGANIZER);
        // απλή μηχανή καταστάσεων (σύμφωνα με αναφορά)
        if (!isValidTransition(f.getState(), next))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid transition: " + f.getState() + " -> " + next);
        f.setState(next);
        return festivals.save(f);
    }

    public Festival view(String id){
        return festivals.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
    }

    public void delete(String id){
        Festival f = festivals.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        roles.ensureRole(username, id, Role.ORGANIZER);
        if (f.getState() != FestivalState.CREATED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Delete allowed only in CREATED");
        festivals.deleteById(id);
    }

    private boolean isValidTransition(FestivalState cur, FestivalState next){
        return switch (cur) {
            case CREATED -> next == FestivalState.SUBMISSION;
            case SUBMISSION -> next == FestivalState.ASSIGNMENT;
            case ASSIGNMENT -> next == FestivalState.REVIEW;
            case REVIEW -> next == FestivalState.SCHEDULING;
            case SCHEDULING -> next == FestivalState.FINAL_SUBMISSION;
            case FINAL_SUBMISSION -> next == FestivalState.DECISION;
            case DECISION -> next == FestivalState.ANNOUNCED;
            case ANNOUNCED -> false;
        };
    }
}
