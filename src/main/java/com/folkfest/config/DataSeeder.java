package com.folkfest.config;

import com.folkfest.model.*;
import com.folkfest.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*;
import java.util.List;

@Configuration
public class DataSeeder {
  @Bean
  CommandLineRunner init(UserRepository users, FestivalRepository festivals, PerformanceRepository performances, RoleAssignmentRepository roles, PasswordEncoder encoder){
    return args -> {
      if(users.count()==0){
        var alice = users.save(com.folkfest.model.User.builder().username("alice").fullName("Alice Folk").passwordHash(encoder.encode("pass")).active(true).build());
        var bob   = users.save(com.folkfest.model.User.builder().username("bob").fullName("Bob Fiddler").passwordHash(encoder.encode("pass")).active(true).build());
        var staff = users.save(com.folkfest.model.User.builder().username("sue").fullName("Sue Stage").passwordHash(encoder.encode("pass")).active(true).build());
        var fest = festivals.save(com.folkfest.model.Festival.builder().name("Folk Roots Festival").description("A celebration of folk music").startDate(LocalDate.now().plusDays(30)).endDate(LocalDate.now().plusDays(33)).venue("Folk Park").state(FestivalState.CREATED).createdAt(Instant.now()).build());

        fest.getOrganizerUserIds().add(alice.getId());
        festivals.save(fest);
        roles.save(new RoleAssignment(null, alice.getId(), fest.getId(), com.folkfest.model.Role.ORGANIZER));

        fest.getStaffUserIds().add(staff.getId());
        festivals.save(fest);
        roles.save(new RoleAssignment(null, staff.getId(), fest.getId(), com.folkfest.model.Role.STAFF));

        performances.save(com.folkfest.model.Performance.builder()
            .festivalId(fest.getId())
            .name("Bob & The Fiddlers")
            .description("Traditional fiddle tunes")
            .genre("Folk")
            .durationMinutes(45)
            .bandMembers(List.of("Bob Fiddler"))
            .artistsUserIds(List.of(bob.getId()))
            .technicalRequirements("2 mics, 1 DI, stage monitors")
            .merchandiseItems(List.of("CD","T-shirt"))
            .setlist(List.of("Tune 1","Tune 2"))
            .preferredRehearsalTimes(List.of("Day -1 afternoon"))
            .preferredPerformanceSlots(List.of("Day 1 evening"))
            .state(PerformanceState.CREATED)
            .createdAt(Instant.now())
            .build());
      }
    };
  }
}
