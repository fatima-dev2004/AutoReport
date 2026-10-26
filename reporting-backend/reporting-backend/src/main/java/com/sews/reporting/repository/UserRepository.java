package com.sews.reporting.repository;

import com.sews.reporting.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    // ============ Méthodes avec pagination (NOUVEAU) ============
    
    /**
     * Récupérer tous les utilisateurs avec pagination
     */
    Page<User> findAll(Pageable pageable);
    
    /**
     * Recherche par email ou nom avec pagination
     */
    Page<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
        String email, String fullName, Pageable pageable
    );
}
