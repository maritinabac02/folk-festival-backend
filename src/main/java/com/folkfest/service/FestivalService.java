package com.folkfest.service;

import com.folkfest.dto.FestivalDtos.*;
import com.folkfest.exception.ApiException;
import com.folkfest.model.*;
import com.folkfest.repo.FestivalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FestivalService {
    private final FestivalRepository festivals;
    private final RoleService roles;

    public FestivalService(FestivalRepository festivals, RoleService roles) {
        this.festivals = festivals; this.roles = roles;
    }

    public Festival create(CreateFestivalRequest r) {
        if (r.endDate.isBefore(r.startDate))
            throw new ApiException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        if (festivals.existsByName(r.name))
            throw new ApiException(HttpStatus.CONFLICT, "Festival name already exists");

        Festival f = new Festival();
        f.setName(r.name);
        f.setDescription(r.description);
        f.setVenue(r.venue);
        f.setStartDate(r.startDate);
        f.setEndDate(r.endDate);
        f.setState(FestivalState.CREATED);
        f = festivals.save(f);

        roles.grantRole(authUser(), f.getId(), Role.ORGANIZER);
        return f;
    }

    public Festival update(String id, UpdateFestivalRequest r) {
        Festival f = view(id);
        roles.ensureRole(authUser(), id, Role.ORGANIZER);

        if (r.name != null && !r.name.equals(f.getName()) && festivals.existsByName(r.name))
            throw new ApiException(HttpStatus.CONFLICT, "Festival name already exists");

        if (r.name != null) f.setName(r.name);
        if (r.description != null) f.setDescription(r.description);
        if (r.venue != null) f.setVenue(r.venue);
        return festivals.save(f);
    }

    public Festival changeState(String id, FestivalState next) {
        Festival f = view(id);
        roles.ensureRole(authUser(), id, Role.ORGANIZER);
        if (!isValidTransition(f.getState(), next))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid transition: " + f.getState() + " -> " + next);
        f.setState(next);
        return festivals.save(f);
    }

    public Festival view(String id) {
        return festivals.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Festival not found"));
    }

    public List<Festival> list() {
        return festivals.findAll();
    }

    public void delete(String id) {
        Festival f = view(id);
        roles.ensureRole(authUser(), id, Role.ORGANIZER);
        if (f.getState() != FestivalState.CREATED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Delete allowed only in CREATED");
        festivals.deleteById(id);
    }

    /** Phases can only move forward one step at a time. */
    private boolean isValidTransition(FestivalState cur, FestivalState next) {
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

    private String authUser() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
}