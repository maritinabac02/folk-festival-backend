package com.folkfest.controller;
import com.folkfest.dto.PerformanceDtos.SearchRequest; import com.folkfest.model.*; import com.folkfest.repo.*; import com.folkfest.service.PerformanceService; import org.springframework.http.ResponseEntity; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.core.userdetails.UserDetails; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/search")
public class SearchController {
  private final PerformanceService performanceService; private final UserRepository userRepository; private final FestivalRepository festivalRepository;
  public SearchController(PerformanceService performanceService, UserRepository userRepository, FestivalRepository festivalRepository){ this.performanceService=performanceService; this.userRepository=userRepository; this.festivalRepository=festivalRepository; }
  @PostMapping("/performances/{festivalId}")
  public ResponseEntity<List<Performance>> searchPerformances(@PathVariable String festivalId, @RequestBody SearchRequest req, @AuthenticationPrincipal UserDetails me){
    Role r = Role.VISITOR; if(me!=null) r = Role.ARTIST; return ResponseEntity.ok(performanceService.search(festivalId, req.name(), req.artists(), req.genre(), r));
  }
}
