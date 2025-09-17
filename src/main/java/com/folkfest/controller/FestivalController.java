package com.folkfest.controller;
import com.folkfest.dto.FestivalDtos.*; import com.folkfest.model.*; import com.folkfest.service.FestivalService; import jakarta.validation.Valid; import org.springframework.http.ResponseEntity; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.core.userdetails.UserDetails; import org.springframework.web.bind.annotation.*; import java.util.Set;
@RestController @RequestMapping("/api/festivals")
public class FestivalController {
  private final FestivalService festivalService; public FestivalController(FestivalService festivalService){ this.festivalService=festivalService; }
  @PostMapping public ResponseEntity<Festival> create(@Valid @RequestBody CreateFestivalRequest req, @AuthenticationPrincipal UserDetails me){ return ResponseEntity.ok(festivalService.createFestival(req, me.getUsername())); }
  @PatchMapping("/{id}") public ResponseEntity<Festival> update(@PathVariable String id, @RequestBody UpdateFestivalRequest req, @AuthenticationPrincipal UserDetails me){ return ResponseEntity.ok(festivalService.updateFestival(id, req, me.getUsername())); }
  @PostMapping("/{id}/state/{next}") public ResponseEntity<Festival> changeState(@PathVariable String id, @PathVariable FestivalState next, @AuthenticationPrincipal UserDetails me){ return ResponseEntity.ok(festivalService.changeState(id, next, me.getUsername())); }
  @PostMapping("/{id}/organizers") public ResponseEntity<Festival> addOrganizers(@PathVariable String id, @RequestBody Set<String> usernames, @AuthenticationPrincipal UserDetails me){ return ResponseEntity.ok(festivalService.addOrganizers(id, usernames, me.getUsername())); }
  @PostMapping("/{id}/staff") public ResponseEntity<Festival> addStaff(@PathVariable String id, @RequestBody Set<String> usernames, @AuthenticationPrincipal UserDetails me){ return ResponseEntity.ok(festivalService.addStaff(id, usernames, me.getUsername())); }
  @DeleteMapping("/{id}") public ResponseEntity<?> deleteFestival(@PathVariable String id, @AuthenticationPrincipal UserDetails me){ festivalService.deleteFestival(id, me.getUsername()); return ResponseEntity.noContent().build(); }
}
