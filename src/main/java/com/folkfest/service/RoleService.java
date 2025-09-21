package com.folkfest.service;

import com.folkfest.exception.ApiException;
import com.folkfest.model.Role;
import com.folkfest.model.RoleAssignment;
import com.folkfest.repo.RoleAssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {
    private final RoleAssignmentRepository userRoles;

    public RoleService(RoleAssignmentRepository userRoles) { this.userRoles = userRoles; }

    public void ensureRole(String username, String festivalId, Role role){
        Optional<RoleAssignment> ur = userRoles.findByUsernameAndFestivalId(username, festivalId);
        if (ur.isEmpty() || ur.get().getRole() != role) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Required role: " + role + " for festival " + festivalId);
        }
    }

    public Role getUserRoleForFestival(String username, String festivalId){
        return userRoles.findByUsernameAndFestivalId(username, festivalId)
                .map(RoleAssignment::getRole).orElse(null);
    }

    public void grantRole(String username, String festivalId, Role role){
        var existing = userRoles.findByUsernameAndFestivalId(username, festivalId);
        if (existing.isPresent()) {
            if (existing.get().getRole() == role) return;
            throw new ApiException(HttpStatus.CONFLICT, "User already has another role in this festival");
        }
        RoleAssignment r = new RoleAssignment();
        r.setUsername(username);
        r.setFestivalId(festivalId);
        r.setRole(role);
        userRoles.save(r);
    }
}
