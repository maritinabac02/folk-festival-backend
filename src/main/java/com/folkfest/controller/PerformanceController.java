package com.folkfest.controller;

import com.folkfest.dto.PerformanceDtos.*;
import com.folkfest.service.PerformanceService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/performances")
public class PerformanceController {
    private final PerformanceService service;

    public PerformanceController(PerformanceService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreatePerformanceRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id,
                                    @Valid @RequestBody UpdatePerformanceRequest req){
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> withdraw(@PathVariable("id") String id){
        service.withdraw(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable("id") String id){
        return ResponseEntity.ok(service.submit(id));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assign(@PathVariable("id") String id,
                                    @RequestParam(name = "staffUsername") String staffUsername){
        return ResponseEntity.ok(service.assignStaff(id, staffUsername));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<?> review(@PathVariable("id") String id,
                                    @Valid @RequestBody ReviewRequest req){
        return ResponseEntity.ok(service.review(id, req));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable("id") String id){
        return ResponseEntity.ok(service.approve(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable("id") String id,
                                    @RequestParam(name = "reason") String reason){
        return ResponseEntity.ok(service.reject(id, reason));
    }

    @PostMapping("/{id}/final-submit")
    public ResponseEntity<?> finalSubmit(@PathVariable("id") String id,
                                         @Valid @RequestBody FinalSubmissionRequest req){
        return ResponseEntity.ok(service.finalSubmit(id, req));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable("id") String id,
                                    @RequestParam(name = "scheduledTime") String scheduledTime,
                                    @RequestParam(name = "scheduledStage") String scheduledStage){
        return ResponseEntity.ok(service.accept(id, scheduledTime, scheduledStage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> view(@PathVariable("id") String id){
        return ResponseEntity.ok(service.view(id));
    }
}
