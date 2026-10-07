package com.folkfest.config;

import com.folkfest.model.*;
import com.folkfest.repo.*;
import com.folkfest.service.RoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates demo users and a sample festival. Runs only with the "dev" profile. */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final FestivalRepository festivals;
    private final PasswordEncoder encoder;
    private final RoleService roles;

    public DataSeeder(UserRepository users, FestivalRepository festivals,
                      PasswordEncoder encoder, RoleService roles) {
        this.users = users; this.festivals = festivals; this.encoder = encoder; this.roles = roles;
    }

    @Override
    public void run(String... args) {
        createUserIfMissing("alice", "Alice Organizer");
        createUserIfMissing("sue", "Sue Staff");
        createUserIfMissing("bob", "Bob Artist");

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

        roles.grantRole("alice", f.getId(), Role.ORGANIZER);
        roles.grantRole("sue", f.getId(), Role.STAFF);
        // bob gets the ARTIST role when he creates his first performance
    }

    private void createUserIfMissing(String username, String fullName) {
        if (users.findByUsername(username).isPresent()) return;
        User u = new User();
        u.setUsername(username);
        u.setEmail(username + "@mail.test");
        u.setFullName(fullName);
        u.setPasswordHash(encoder.encode("pass"));
        u.setActive(true);
        users.save(u);
    }
}