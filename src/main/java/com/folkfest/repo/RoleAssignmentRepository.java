package com.folkfest.repo;
import com.folkfest.model.*; import org.springframework.data.mongodb.repository.MongoRepository; import java.util.*;
public interface RoleAssignmentRepository extends MongoRepository<RoleAssignment, String> {
  List<RoleAssignment> findByUserId(String userId);
  List<RoleAssignment> findByFestivalIdAndRole(String festivalId, Role role);
  Optional<RoleAssignment> findByUserIdAndFestivalId(String userId, String festivalId);
}
