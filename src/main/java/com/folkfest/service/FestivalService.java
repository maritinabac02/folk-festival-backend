package com.folkfest.service;
import com.folkfest.dto.FestivalDtos.*; import com.folkfest.exception.ApiException; import com.folkfest.model.*; import com.folkfest.repo.*; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import java.time.Instant; import java.util.Set;
@Service
public class FestivalService {
  private final FestivalRepository festivalRepository; private final UserRepository userRepository; private final RoleService roleService; private final RoleAssignmentRepository roleAssignmentRepository;
  public FestivalService(FestivalRepository festivalRepository, UserRepository userRepository, RoleService roleService, RoleAssignmentRepository roleAssignmentRepository){ this.festivalRepository=festivalRepository; this.userRepository=userRepository; this.roleService=roleService; this.roleAssignmentRepository=roleAssignmentRepository; }
  public Festival createFestival(CreateFestivalRequest req, String creatorUsername){
    festivalRepository.findByNameIgnoreCase(req.name()).ifPresent(f->{ throw new ApiException(HttpStatus.CONFLICT,"Festival name already exists"); });
    var creator=userRepository.findByUsername(creatorUsername).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Creator user not found"));
    Festival f=Festival.builder().name(req.name()).description(req.description()).startDate(req.startDate()).endDate(req.endDate()).venue(req.venue()).state(FestivalState.CREATED).createdAt(Instant.now()).build();
    f=festivalRepository.save(f); f.getOrganizerUserIds().add(creator.getId()); festivalRepository.save(f); roleService.grantRole(creator.getId(), f.getId(), Role.ORGANIZER); return f;
  }
  public Festival updateFestival(String festivalId, UpdateFestivalRequest req, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); Festival f=festivalRepository.findById(festivalId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Festival not found"));
    roleService.ensureRole(user.getId(), festivalId, Role.ORGANIZER);
    if(req.description()!=null) f.setDescription(req.description()); if(req.startDate()!=null) f.setStartDate(req.startDate()); if(req.endDate()!=null) f.setEndDate(req.endDate());
    if(req.venue()!=null) f.setVenue(req.venue()); if(req.venueLayout()!=null) f.setVenueLayout(req.venueLayout()); if(req.budgetInfo()!=null) f.setBudgetInfo(req.budgetInfo()); if(req.vendorInfo()!=null) f.setVendorInfo(req.vendorInfo());
    return festivalRepository.save(f);
  }
  public Festival changeState(String festivalId, FestivalState next, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); Festival f=festivalRepository.findById(festivalId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Festival not found"));
    roleService.ensureRole(user.getId(), festivalId, Role.ORGANIZER);
    switch(next){ case SUBMISSION -> requireState(f, FestivalState.CREATED); case ASSIGNMENT -> requireState(f, FestivalState.SUBMISSION);
      case REVIEW -> requireState(f, FestivalState.ASSIGNMENT); case SCHEDULING -> requireState(f, FestivalState.REVIEW);
      case FINAL_SUBMISSION -> requireState(f, FestivalState.SCHEDULING); case DECISION -> requireState(f, FestivalState.FINAL_SUBMISSION);
      case ANNOUNCED -> requireState(f, FestivalState.DECISION); default -> throw new ApiException(HttpStatus.BAD_REQUEST,"Invalid next state");}
    f.setState(next); return festivalRepository.save(f);
  }
  private void requireState(Festival f, FestivalState expected){ if(f.getState()!=expected) throw new ApiException(HttpStatus.BAD_REQUEST,"Festival must be in state "+expected); }
  public Festival addOrganizers(String festivalId, Set<String> usernames, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); Festival f=festivalRepository.findById(festivalId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Festival not found"));
    roleService.ensureRole(user.getId(), festivalId, Role.ORGANIZER);
    for(String un:usernames){ var u=userRepository.findByUsername(un).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"User not found: "+un)); f.getOrganizerUserIds().add(u.getId()); roleService.grantRole(u.getId(), f.getId(), Role.ORGANIZER); }
    return festivalRepository.save(f);
  }
  public Festival addStaff(String festivalId, Set<String> usernames, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); Festival f=festivalRepository.findById(festivalId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Festival not found"));
    roleService.ensureRole(user.getId(), festivalId, Role.ORGANIZER);
    for(String un:usernames){ var u=userRepository.findByUsername(un).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"User not found: "+un)); f.getStaffUserIds().add(u.getId()); roleService.grantRole(u.getId(), f.getId(), Role.STAFF); }
    return festivalRepository.save(f);
  }
  public void deleteFestival(String festivalId, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); Festival f=festivalRepository.findById(festivalId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Festival not found"));
    roleService.ensureRole(user.getId(), festivalId, Role.ORGANIZER); if(f.getState()!=FestivalState.CREATED) throw new ApiException(HttpStatus.BAD_REQUEST,"Festival deletion allowed only in CREATED");
    festivalRepository.deleteById(festivalId);
  }
}
