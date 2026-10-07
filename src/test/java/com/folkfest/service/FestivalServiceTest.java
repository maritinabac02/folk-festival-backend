package com.folkfest.service;

import com.folkfest.exception.ApiException;
import com.folkfest.model.Festival;
import com.folkfest.model.FestivalState;
import com.folkfest.model.Role;
import com.folkfest.repo.FestivalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FestivalServiceTest {

    private FestivalRepository festivals;
    private RoleService roles;
    private FestivalService service;

    @BeforeEach
    void setUp() {
        festivals = mock(FestivalRepository.class);
        roles = mock(RoleService.class);
        service = new FestivalService(festivals, roles);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null));
        when(festivals.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Festival festivalIn(FestivalState state) {
        Festival f = new Festival();
        f.setState(state);
        when(festivals.findById("f1")).thenReturn(Optional.of(f));
        return f;
    }

    @Test
    void movesToNextState() {
        festivalIn(FestivalState.CREATED);

        Festival result = service.changeState("f1", FestivalState.SUBMISSION);

        assertEquals(FestivalState.SUBMISSION, result.getState());
    }

    @Test
    void cannotSkipStates() {
        festivalIn(FestivalState.CREATED);

        assertThrows(ApiException.class,
                () -> service.changeState("f1", FestivalState.REVIEW));
    }

    @Test
    void onlyOrganizerCanChangeState() {
        festivalIn(FestivalState.CREATED);
        doThrow(new ApiException(HttpStatus.FORBIDDEN, "not organizer"))
                .when(roles).ensureRole("alice", "f1", Role.ORGANIZER);

        assertThrows(ApiException.class,
                () -> service.changeState("f1", FestivalState.SUBMISSION));
        verify(festivals, never()).save(any());
    }

    @Test
    void cannotDeleteAfterSubmissionStarted() {
        festivalIn(FestivalState.SUBMISSION);

        assertThrows(ApiException.class, () -> service.delete("f1"));
        verify(festivals, never()).deleteById(any());
    }
}