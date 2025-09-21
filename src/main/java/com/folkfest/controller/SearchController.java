package com.folkfest.controller;

import com.folkfest.dto.PerformanceDtos.SearchRequest;
import com.folkfest.model.Role;
import com.folkfest.service.RoleService;
import com.folkfest.repo.PerformanceRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final PerformanceRepository perfRepo;
    private final RoleService roles;

    public SearchController(PerformanceRepository perfRepo, RoleService roles) {
        this.perfRepo = perfRepo; this.roles = roles;
    }

    @PostMapping("/performances/{festivalId}")
    public ResponseEntity<?> searchPerformances(@PathVariable("festivalId") String festivalId,
                                                @Valid @RequestBody SearchRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var role = roles.getUserRoleForFestival(username, festivalId);
        var all = perfRepo.searchByCriteria(festivalId, req.name, req.artists, req.genre);
        if (role == null) {
            all.removeIf(p -> p.getState() != com.folkfest.model.PerformanceState.SCHEDULED);
            all.forEach(p -> p.stripSensitiveForVisitor());
        }
        return ResponseEntity.ok(all);
    }
}

