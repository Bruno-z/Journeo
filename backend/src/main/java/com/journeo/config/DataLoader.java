package com.journeo.config;

import com.journeo.model.User;
import com.journeo.repository.GuideRepository;
import com.journeo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GuideRepository guideRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, GuideRepository guideRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.guideRepository = guideRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        User admin = userRepository.save(new User("admin@hws.com", passwordEncoder.encode("admin123"), User.Role.ADMIN));
        User user1 = userRepository.save(new User("user1@hws.com", passwordEncoder.encode("user123"), User.Role.USER));
        User user2 = userRepository.save(new User("user2@hws.com", passwordEncoder.encode("user123"), User.Role.USER));

        // Associate guides with users (guides 1-8 inserted by V2 Flyway migration)
        // Mirrors original V2 guide_user associations:
        // (1,admin), (1,user1), (2,admin), (3,admin), (4,user1), (5,user2), (6,admin), (7,user2), (8,user2)
        assignUsers(1L, admin, user1);
        assignUsers(2L, admin);
        assignUsers(3L, admin);
        assignUsers(4L, user1);
        assignUsers(5L, user2);
        assignUsers(6L, admin);
        assignUsers(7L, user2);
        assignUsers(8L, user2);

        System.out.println("DataLoader: utilisateurs et associations guides créés.");
    }

    private void assignUsers(Long guideId, User... users) {
        guideRepository.findById(guideId).ifPresent(guide -> {
            for (User user : users) {
                guide.addUser(user);
            }
            guideRepository.save(guide);
        });
    }
}
