package com.folkfest.controller;

import com.folkfest.dto.FestivalDtos.*;
import com.folkfest.exception.ApiException;
import com.folkfest.model.FestivalState;
import com.folkfest.service.FestivalService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/festivals")
public class FestivalController {
    private final FestivalService service;

    public FestivalController(FestivalService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateFestivalRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id,
                                    @Valid @RequestBody UpdateFestivalRequest req){
        return ResponseEntity.ok(service.update(id, req));
    }

    
    @PatchMapping("/{id}/state")
    public ResponseEntity<?> changeStateBody(@PathVariable("id") String id,
                                             @Valid @RequestBody ChangeFestivalStateRequest req){
        FestivalState next;
        try { next = FestivalState.valueOf(req.next); }
        catch (IllegalArgumentException e){ throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid state: " + req.next); }
        return ResponseEntity.ok(service.changeState(id, next));
    }

    
    @PostMapping("/{id}/state/{next}")
    public ResponseEntity<?> changeStatePath(@PathVariable("id") String id,
                                             @PathVariable("next") String next){
        FestivalState st;
        try { st = FestivalState.valueOf(next); }
        catch (IllegalArgumentException e){ throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid state: " + next); }
        return ResponseEntity.ok(service.changeState(id, st));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> view(@PathVariable("id") String id){
        return ResponseEntity.ok(service.view(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

