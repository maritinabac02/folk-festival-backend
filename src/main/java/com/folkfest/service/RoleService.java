package com.folkfest.service;
import com.folkfest.exception.ApiException; import com.folkfest.model.*; import com.folkfest.repo.RoleAssignmentRepository; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service;
@Service
public class RoleService {
  private final RoleAssignmentRepository repo; public RoleService(RoleAssignmentRepository repo){ this.repo=repo; }
  public Role getUserRoleForFestival(String userId,String festivalId){ return repo.findByUserIdAndFestivalId(userId, festivalId).map(RoleAssignment::getRole).orElse(null); }
  public void ensureRole(String userId,String festivalId,Role... allowed){ Role r=getUserRoleForFestival(userId,festivalId); for(Role ar:allowed) if(ar==r) return; throw new ApiException(HttpStatus.FORBIDDEN,"Insufficient role for this festival"); }
  public void grantRole(String userId,String festivalId,Role role){ var ra=repo.findByUserIdAndFestivalId(userId,festivalId).orElse(RoleAssignment.builder().userId(userId).festivalId(festivalId).role(role).build()); ra.setRole(role); repo.save(ra); }
}
