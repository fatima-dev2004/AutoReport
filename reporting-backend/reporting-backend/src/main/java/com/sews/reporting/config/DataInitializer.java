package com.sews.reporting.config;

import com.sews.reporting.entity.Role;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.UserRepository;
import com.sews.reporting.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cree un compte administrateur par defaut au premier demarrage,
 * afin de pouvoir se connecter et creer les autres utilisateurs (section 4/5.1).
 *
 * Identifiants par defaut : admin@sews.com / Admin@123
 * A CHANGER IMPERATIVEMENT en production.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemSettingsService systemSettingsService;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .email("admin@sews.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("Administrateur")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build());

            userRepository.save(User.builder()
                    .email("manager@sews.com")
                    .password(passwordEncoder.encode("Manager@123"))
                    .fullName("Responsable Production")
                    .role(Role.MANAGER)
                    .enabled(true)
                    .build());

            userRepository.save(User.builder()
                    .email("employee@sews.com")
                    .password(passwordEncoder.encode("Employee@123"))
                    .fullName("Employe Consultation")
                    .role(Role.USER)
                    .enabled(true)
                    .build());

            log.info("Comptes de demonstration crees : admin@sews.com, manager@sews.com, employee@sews.com");
        }
        
        // Initialize default system settings
        systemSettingsService.initializeDefaultSettings();
        log.info("Parametres systeme initialises");
    }
}
