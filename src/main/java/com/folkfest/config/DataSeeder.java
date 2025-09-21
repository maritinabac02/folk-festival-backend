package com.folkfest.config;

import com.folkfest.model.*;
import com.folkfest.repo.*;
import com.folkfest.service.RoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final FestivalRepository festivals;
    private final PerformanceRepository perfs;
    private final PasswordEncoder encoder;
    private final RoleService roles;

    public DataSeeder(UserRepository users, FestivalRepository festivals,
                      PerformanceRepository perfs, PasswordEncoder encoder, RoleService roles) {
        this.users = users; this.festivals = festivals; this.perfs = perfs; this.encoder = encoder; this.roles = roles;
    }

    @Override
    public void run(String... args) {
        // users
        if (users.findByUsername("alice").isEmpty()) {
            var u = new User(); u.setUsername("alice"); u.setEmail("alice@mail.test");
            u.setFullName("Alice Organizer"); u.setPasswordHash(encoder.encode("pass")); u.setActive(true);
            users.save(u);
        }
        if (users.findByUsername("sue").isEmpty()) {
            var u = new User(); u.setUsername("sue"); u.setEmail("sue@mail.test");
            u.setFullName("Sue Staff"); u.setPasswordHash(encoder.encode("pass")); u.setActive(true);
            users.save(u);
        }
        if (users.findByUsername("bob").isEmpty()) {
            var u = new User(); u.setUsername("bob"); u.setEmail("bob@mail.test");
            u.setFullName("Bob Artist"); u.setPasswordHash(encoder.encode("pass")); u.setActive(true);
            users.save(u);
        }

        // 1 sample festival
        Festival f = festivals.findFirstByOrderByStartDateAsc().orElseGet(() -> {
            Festival nf = new Festival();
            nf.setName("FolkFest 2025");
            nf.setDescription("Seeded festival");
            nf.setVenue("Main Park");
            nf.setStartDate(java.time.LocalDate.now().plusDays(10));
            nf.setEndDate(java.time.LocalDate.now().plusDays(12));
            nf.setState(FestivalState.CREATED);
            return festivals.save(nf);
        });

        // roles
        roles.grantRole("alice", f.getId(), Role.ORGANIZER);
        roles.grantRole("sue", f.getId(), Role.STAFF);
        // ο bob θα γίνει ARTIST όταν δημιουργήσει performance
    }
}

