package com.folkfest.service;
import com.folkfest.dto.PerformanceDtos.*; import com.folkfest.exception.ApiException; import com.folkfest.model.*; import com.folkfest.repo.*; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service;
import java.time.Instant; import java.util.*; import java.util.stream.Collectors;
@Service
public class PerformanceService {
  private final PerformanceRepository performanceRepository; private final FestivalRepository festivalRepository; private final UserRepository userRepository; private final RoleService roleService; private final RoleAssignmentRepository roleAssignmentRepository;
  public PerformanceService(PerformanceRepository performanceRepository, FestivalRepository festivalRepository, UserRepository userRepository, RoleService roleService, RoleAssignmentRepository roleAssignmentRepository){
    this.performanceRepository=performanceRepository; this.festivalRepository=festivalRepository; this.userRepository=userRepository; this.roleService=roleService; this.roleAssignmentRepository=roleAssignmentRepository; }
  public Performance createPerformance(CreatePerformanceRequest req, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); var festival=festivalRepository.findById(req.festivalId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Festival not found"));
    if(performanceRepository.findByFestivalIdAndNameIgnoreCase(req.festivalId(), req.name()).isPresent()) throw new ApiException(HttpStatus.CONFLICT,"Performance name already exists in festival");
    if(festival.getState()==FestivalState.ANNOUNCED) throw new ApiException(HttpStatus.BAD_REQUEST,"Festival is locked");
    Performance p=Performance.builder().festivalId(req.festivalId()).name(req.name()).description(req.description()).genre(req.genre()).durationMinutes(req.durationMinutes())
      .artistsUserIds(new ArrayList<>(java.util.List.of(user.getId()))).bandMembers(new ArrayList<>(java.util.List.of(user.getFullName()))).state(PerformanceState.CREATED).createdAt(Instant.now()).build();
    p=performanceRepository.save(p); roleService.grantRole(user.getId(), req.festivalId(), Role.ARTIST); return p;
  }
  public Performance updatePerformance(String performanceId, UpdatePerformanceRequest req, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); Performance p=performanceRepository.findById(performanceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Performance not found"));
    roleService.ensureRole(user.getId(), p.getFestivalId(), Role.ARTIST); if(!p.getArtistsUserIds().contains(user.getId())) throw new ApiException(HttpStatus.FORBIDDEN,"Only artists of this performance can update it");
    if(p.getState()!=PerformanceState.CREATED && p.getState()!=PerformanceState.SUBMITTED) throw new ApiException(HttpStatus.BAD_REQUEST,"Updates allowed only before final submission");
    if(req.name()!=null) p.setName(req.name()); if(req.description()!=null) p.setDescription(req.description()); if(req.genre()!=null) p.setGenre(req.genre()); if(req.durationMinutes()!=null) p.setDurationMinutes(req.durationMinutes());
    if(req.bandMembers()!=null) p.setBandMembers(req.bandMembers()); if(req.technicalRequirements()!=null) p.setTechnicalRequirements(req.technicalRequirements()); if(req.merchandiseItems()!=null) p.setMerchandiseItems(req.merchandiseItems());
    if(req.setlist()!=null) p.setSetlist(req.setlist()); if(req.preferredRehearsalTimes()!=null) p.setPreferredRehearsalTimes(req.preferredRehearsalTimes()); if(req.preferredPerformanceSlots()!=null) p.setPreferredPerformanceSlots(req.preferredPerformanceSlots());
    return performanceRepository.save(p);
  }
  public void addBandManager(String performanceId, String targetUsername, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); var target=userRepository.findByUsername(targetUsername).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"User not found"));
    Performance p=performanceRepository.findById(performanceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Performance not found")); roleService.ensureRole(user.getId(), p.getFestivalId(), Role.ARTIST);
    if(!p.getArtistsUserIds().contains(user.getId())) throw new ApiException(HttpStatus.FORBIDDEN,"Only main artist can add band manager"); if(!p.getBandMembers().contains(target.getFullName())) throw new ApiException(HttpStatus.BAD_REQUEST,"Target user must be included in band members");
    p.getArtistsUserIds().add(target.getId()); performanceRepository.save(p); roleService.grantRole(target.getId(), p.getFestivalId(), Role.ARTIST);
  }
  public Performance submitPerformance(String performanceId, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); Performance p=performanceRepository.findById(performanceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Performance not found"));
    var festival=festivalRepository.findById(p.getFestivalId()).orElseThrow(); roleService.ensureRole(user.getId(), p.getFestivalId(), Role.ARTIST);
    if(!p.getArtistsUserIds().contains(user.getId())) throw new ApiException(HttpStatus.FORBIDDEN,"Only artists of this performance can submit it"); if(festival.getState()!=FestivalState.SUBMISSION) throw new ApiException(HttpStatus.BAD_REQUEST,"Festival must be in SUBMISSION");
    if(isEmpty(p.getName()) || isEmpty(p.getDescription()) || isEmpty(p.getGenre()) || p.getDurationMinutes() <= 0 || p.getBandMembers().isEmpty()
      || isEmpty(p.getTechnicalRequirements()) || p.getSetlist().isEmpty() || p.getMerchandiseItems().isEmpty() || p.getPreferredRehearsalTimes().isEmpty() || p.getPreferredPerformanceSlots().isEmpty())
      throw new ApiException(HttpStatus.BAD_REQUEST,"Incomplete performance details for submission");
    p.setState(PerformanceState.SUBMITTED); return performanceRepository.save(p);
  }
  public void withdrawPerformance(String performanceId, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); Performance p=performanceRepository.findById(performanceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Performance not found"));
    roleService.ensureRole(user.getId(), p.getFestivalId(), Role.ARTIST); if(!p.getArtistsUserIds().contains(user.getId())) throw new ApiException(HttpStatus.FORBIDDEN,"Only artists can withdraw their performance");
    if(p.getState()!=PerformanceState.CREATED) throw new ApiException(HttpStatus.BAD_REQUEST,"Can withdraw only before SUBMITTED"); performanceRepository.deleteById(performanceId);
  }
  public Performance assignStaff(String performanceId, AssignStaffRequest req, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); Performance p=performanceRepository.findById(performanceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Performance not found"));
    var festival=festivalRepository.findById(p.getFestivalId()).orElseThrow(); roleService.ensureRole(user.getId(), p.getFestivalId(), Role.ORGANIZER);
    if(festival.getState()!=FestivalState.ASSIGNMENT) throw new ApiException(HttpStatus.BAD_REQUEST,"Festival must be in ASSIGNMENT"); if(!festival.getStaffUserIds().contains(req.staffUserId())) throw new ApiException(HttpStatus.BAD_REQUEST,"User is not STAFF for this festival");
    p.setAssignedStaffUserId(req.staffUserId()); return performanceRepository.save(p);
  }
  public Performance review(String performanceId, ReviewRequest req, String username){
    var staff=userRepository.findByUsername(username).orElseThrow(); Performance p=performanceRepository.findById(performanceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Performance not found"));
    var festival=festivalRepository.findById(p.getFestivalId()).orElseThrow(); roleService.ensureRole(staff.getId(), p.getFestivalId(), Role.STAFF);
    if(!staff.getId().equals(p.getAssignedStaffUserId())) throw new ApiException(HttpStatus.FORBIDDEN,"Only assigned STAFF can review"); if(festival.getState()!=FestivalState.REVIEW) throw new ApiException(HttpStatus.BAD_REQUEST,"Festival must be in REVIEW");
    if(p.getState()!=PerformanceState.SUBMITTED) throw new ApiException(HttpStatus.BAD_REQUEST,"Performance must be SUBMITTED to review"); p.setReview(new Review(staff.getId(), req.score(), req.comments())); p.setState(PerformanceState.REVIEWED); return performanceRepository.save(p);
  }
  public Performance approve(String performanceId, String username){
    var org=userRepository.findByUsername(username).orElseThrow(); Performance p=performanceRepository.findById(performanceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Performance not found"));
    var festival=festivalRepository.findById(p.getFestivalId()).orElseThrow(); roleService.ensureRole(org.getId(), p.getFestivalId(), Role.ORGANIZER);
    if(festival.getState()!=FestivalState.SCHEDULING) throw new ApiException(HttpStatus.BAD_REQUEST,"Festival must be in SCHEDULING"); if(p.getState()!=PerformanceState.REVIEWED) throw new ApiException(HttpStatus.BAD_REQUEST,"Performance must be REVIEWED to approve");
    p.setState(PerformanceState.APPROVED); return performanceRepository.save(p);
  }
  public Performance reject(String performanceId, String reason, String username){
    var org=userRepository.findByUsername(username).orElseThrow(); Performance p=performanceRepository.findById(performanceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Performance not found"));
    var festival=festivalRepository.findById(p.getFestivalId()).orElseThrow(); roleService.ensureRole(org.getId(), p.getFestivalId(), Role.ORGANIZER);
    if(!(festival.getState()==FestivalState.SCHEDULING || festival.getState()==FestivalState.DECISION)) throw new ApiException(HttpStatus.BAD_REQUEST,"Manual rejection allowed in SCHEDULING or DECISION");
    p.setState(PerformanceState.REJECTED); p.setRejectionReason((reason==null||reason.isBlank())?"Rejected":reason); return performanceRepository.save(p);
  }
  public Performance finalSubmission(String performanceId, FinalSubmissionRequest req, String username){
    var user=userRepository.findByUsername(username).orElseThrow(); Performance p=performanceRepository.findById(performanceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Performance not found"));
    var festival=festivalRepository.findById(p.getFestivalId()).orElseThrow(); roleService.ensureRole(user.getId(), p.getFestivalId(), Role.ARTIST);
    if(!p.getArtistsUserIds().contains(user.getId())) throw new ApiException(HttpStatus.FORBIDDEN,"Only artists can final-submit"); if(festival.getState()!=FestivalState.FINAL_SUBMISSION) throw new ApiException(HttpStatus.BAD_REQUEST,"Festival must be in FINAL_SUBMISSION");
    if(p.getState()!=PerformanceState.APPROVED) throw new ApiException(HttpStatus.BAD_REQUEST,"Only APPROVED performance can final-submit");
    if(req.setlist()==null||req.setlist().isEmpty()||req.preferredRehearsalTimes()==null||req.preferredRehearsalTimes().isEmpty()||req.preferredPerformanceSlots()==null||req.preferredPerformanceSlots().isEmpty())
      throw new ApiException(HttpStatus.BAD_REQUEST,"Missing final submission details");
    p.setSetlist(req.setlist()); p.setPreferredRehearsalTimes(req.preferredRehearsalTimes()); p.setPreferredPerformanceSlots(req.preferredPerformanceSlots()); return performanceRepository.save(p);
  }
  public Performance acceptAndSchedule(String performanceId, String scheduledTime, String scheduledStage, String username){
    var org=userRepository.findByUsername(username).orElseThrow(); Performance p=performanceRepository.findById(performanceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Performance not found"));
    var festival=festivalRepository.findById(p.getFestivalId()).orElseThrow(); roleService.ensureRole(org.getId(), p.getFestivalId(), Role.ORGANIZER);
    if(festival.getState()!=FestivalState.DECISION) throw new ApiException(HttpStatus.BAD_REQUEST,"Festival must be in DECISION"); if(p.getState()!=PerformanceState.APPROVED) throw new ApiException(HttpStatus.BAD_REQUEST,"Performance must be APPROVED to accept");
    p.setScheduledTime(scheduledTime); p.setScheduledStage(scheduledStage); p.setState(PerformanceState.SCHEDULED); return performanceRepository.save(p);
  }
  public java.util.List<Performance> search(String festivalId, String name, String artists, String genre, Role role){
    java.util.List<Performance> all=performanceRepository.findByFestivalId(festivalId);
    return all.stream().filter(p->{ boolean ok=true;
      if(name!=null && !name.isBlank()) for(String w:name.toLowerCase().split("\s+")) ok &= p.getName()!=null && p.getName().toLowerCase().contains(w);
      if(genre!=null && !genre.isBlank()) for(String w:genre.toLowerCase().split("\s+")) ok &= p.getGenre()!=null && p.getGenre().toLowerCase().contains(w);
      if(artists!=null && !artists.isBlank()) for(String w:artists.toLowerCase().split("\s+")){ boolean any=p.getBandMembers().stream().anyMatch(b->b!=null && b.toLowerCase().contains(w)); ok &= any; }
      return ok; }).sorted(java.util.Comparator.comparing(Performance::getGenre, java.util.Comparator.nullsLast(String::compareToIgnoreCase)).thenComparing(Performance::getName, java.util.Comparator.nullsLast(String::compareToIgnoreCase)))
      .map(p->filterByRoleView(p, role)).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toList());
  }
  private Performance filterByRoleView(Performance p, Role role){ if(role==Role.VISITOR){ if(p.getState()!=PerformanceState.SCHEDULED) return null; return p; } return p; }
  private boolean isEmpty(String s){ return s==null||s.isBlank(); }
}
