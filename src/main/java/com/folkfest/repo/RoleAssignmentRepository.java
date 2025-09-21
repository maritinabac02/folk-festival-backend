package com.folkfest.repo;

import com.folkfest.model.RoleAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleAssignmentRepository extends MongoRepository<RoleAssignment, String> {
    Optional<RoleAssignment> findByUsernameAndFestivalId(String username, String festivalId);
}
