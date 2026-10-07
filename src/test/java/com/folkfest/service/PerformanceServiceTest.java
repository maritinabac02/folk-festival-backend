package com.folkfest.service;

import com.folkfest.dto.PerformanceDtos.ReviewRequest;
import com.folkfest.exception.ApiException;
import com.folkfest.model.*;
import com.folkfest.repo.FestivalRepository;
import com.folkfest.repo.PerformanceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PerformanceServiceTest {

    private PerformanceRepository perfRepo;
    private FestivalRepository festRepo;
    private RoleService roles;
    private PerformanceService service;

    private Performance performance;
    private Festival festival;

    @BeforeEach
    void setUp() {
        perfRepo = mock(PerformanceRepository.class);
        festRepo = mock(FestivalRepository.class);
        roles = mock(RoleService.class);
        service = new PerformanceService(perfRepo, festRepo, roles);

        festival = new Festival();
        festival.setId("f1");
        performance = new Performance();
        performance.setId("p1");
        performance.setFestivalId("f1");
        performance.setMainArtist("bob");

        when(festRepo.findById("f1")).thenReturn(Optional.of(festival));
        when(perfRepo.findById("p1")).thenReturn(Optional.of(performance));
        when(perfRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null));
    }

    private ReviewRequest review(int score) {
        ReviewRequest r = new ReviewRequest();
        r.score = score;
        r.comments = "Good energy";
        return r;
    }

    @Test
    void submitFailsWhenDetailsAreMissing() {
        loginAs("bob");
        festival.setState(FestivalState.SUBMISSION);
        performance.setName("Duo");
        // no description, setlist, etc.

        assertThrows(ApiException.class, () -> service.submit("p1"));
    }

    @Test
    void onlyAssignedStaffCanReview() {
        loginAs("someone-else");
        festival.setState(FestivalState.REVIEW);
        performance.setState(PerformanceState.SUBMITTED);
        performance.setAssignedStaff("sue");

        assertThrows(ApiException.class, () -> service.review("p1", review(80)));
    }

    @Test
    void assignedStaffReviewIsStored() {
        loginAs("sue");
        festival.setState(FestivalState.REVIEW);
        performance.setState(PerformanceState.SUBMITTED);
        performance.setAssignedStaff("sue");

        Performance result = service.review("p1", review(80));

        assertEquals(PerformanceState.REVIEWED, result.getState());
        assertEquals(80, result.getReview().getScore());
    }

    @Test
    void visitorCannotSeeUnscheduledPerformance() {
        loginAs("visitor");
        when(roles.getUserRoleForFestival("visitor", "f1")).thenReturn(null);
        performance.setState(PerformanceState.REVIEWED);

        assertThrows(ApiException.class, () -> service.view("p1"));
    }

    @Test
    void visitorDoesNotSeeReviewOfScheduledPerformance() {
        loginAs("visitor");
        when(roles.getUserRoleForFestival("visitor", "f1")).thenReturn(null);
        performance.setState(PerformanceState.SCHEDULED);
        performance.setReview(new Review(90, "secret notes"));

        Performance result = service.view("p1");

        assertNull(result.getReview());
    }
}