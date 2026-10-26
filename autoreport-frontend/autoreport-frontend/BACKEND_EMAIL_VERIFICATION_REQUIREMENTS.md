# Backend Email Verification Requirements

## 📋 Vue d'ensemble

Ce document décrit les exigences backend pour implémenter la **vérification par email avant envoi de rapports**.

## 🎯 Fonctionnalité

Avant d'envoyer un rapport par email, l'utilisateur doit vérifier son adresse email avec un code à 6 chiffres envoyé par email.

### Workflow Complet

```
1. Utilisateur clique "Envoyer par email" sur un rapport
   ↓
2. Frontend demande l'email et envoie POST /api/email/send-verification
   ↓
3. Backend génère un code à 6 chiffres, l'envoie par email
   ↓
4. Utilisateur reçoit l'email et entre le code
   ↓
5. Frontend envoie POST /api/email/verify-and-send avec le code
   ↓
6. Backend vérifie le code et envoie le rapport par email
   ↓
7. Succès ! Le rapport est dans la boîte mail de l'utilisateur
```

---

## 🗄️ 1. Entité `EmailVerification`

### Fichier : `EmailVerification.java`

```java
package com.sews.autoreport.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Data
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String verificationId; // UUID unique

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code; // Code à 6 chiffres (stocké hashé)

    @Column(nullable = false)
    private Long reportId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt; // 5 minutes après creation

    @Column(nullable = false)
    private Integer attempts = 0; // Nombre de tentatives (max 3)

    @Column(nullable = false)
    private String status; // PENDING, VERIFIED, EXPIRED, BLOCKED

    @Column
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusMinutes(5);
        status = "PENDING";
    }
}
```

### Script SQL de création

```sql
CREATE TABLE email_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    verification_id VARCHAR(36) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    report_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    attempts INT DEFAULT 0 NOT NULL,
    status VARCHAR(20) NOT NULL,
    verified_at DATETIME,
    
    INDEX idx_verification_id (verification_id),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at),
    
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE
);
```

---

## 📦 2. Repository

### Fichier : `EmailVerificationRepository.java`

```java
package com.sews.autoreport.repository;

import com.sews.autoreport.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    Optional<EmailVerification> findByVerificationId(String verificationId);
    
    Optional<EmailVerification> findByVerificationIdAndStatus(String verificationId, String status);
    
    // Nettoyer les vérifications expirées (tâche planifiée)
    void deleteByExpiresAtBeforeAndStatusNot(LocalDateTime dateTime, String status);
}
```

---

## 🔧 3. Service

### Fichier : `EmailVerificationService.java`

```java
package com.sews.autoreport.service;

import com.sews.autoreport.model.EmailVerification;
import com.sews.autoreport.model.Report;
import com.sews.autoreport.repository.EmailVerificationRepository;
import com.sews.autoreport.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepo;
    private final ReportRepository reportRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 3;
    private static final int EXPIRY_MINUTES = 5;

    /**
     * Génère et envoie un code de vérification
     */
    @Transactional
    public EmailVerification sendVerificationCode(String email, Long reportId) {
        // Vérifier que le rapport existe
        Report report = reportRepo.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Rapport introuvable"));

        // Générer un code à 6 chiffres
        String code = generateCode();
        
        // Créer la vérification
        EmailVerification verification = new EmailVerification();
        verification.setVerificationId(UUID.randomUUID().toString());
        verification.setEmail(email);
        verification.setCode(passwordEncoder.encode(code)); // Hash du code
        verification.setReportId(reportId);
        
        verification = verificationRepo.save(verification);
        
        // Envoyer l'email avec le code
        emailService.sendVerificationCode(email, code, report.getTitle());
        
        return verification;
    }

    /**
     * Vérifie le code et envoie le rapport
     */
    @Transactional
    public void verifyAndSendReport(String verificationId, String code) {
        EmailVerification verification = verificationRepo
            .findByVerificationId(verificationId)
            .orElseThrow(() -> new RuntimeException("Vérification introuvable"));

        // Vérifications de sécurité
        if (!"PENDING".equals(verification.getStatus())) {
            throw new RuntimeException("Cette vérification n'est plus valide");
        }

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            verification.setStatus("EXPIRED");
            verificationRepo.save(verification);
            throw new RuntimeException("Le code a expiré");
        }

        if (verification.getAttempts() >= MAX_ATTEMPTS) {
            verification.setStatus("BLOCKED");
            verificationRepo.save(verification);
            throw new RuntimeException("Trop de tentatives. Veuillez redemander un code.");
        }

        // Vérifier le code
        verification.setAttempts(verification.getAttempts() + 1);
        verificationRepo.save(verification);

        if (!passwordEncoder.matches(code, verification.getCode())) {
            throw new RuntimeException("Code incorrect. Il vous reste " + 
                (MAX_ATTEMPTS - verification.getAttempts()) + " tentative(s)");
        }

        // Code correct ! Marquer comme vérifié
        verification.setStatus("VERIFIED");
        verification.setVerifiedAt(LocalDateTime.now());
        verificationRepo.save(verification);

        // Envoyer le rapport par email
        Report report = reportRepo.findById(verification.getReportId())
            .orElseThrow(() -> new RuntimeException("Rapport introuvable"));
        
        emailService.sendReportByEmail(verification.getEmail(), report);
    }

    /**
     * Renvoie un nouveau code
     */
    @Transactional
    public EmailVerification resendVerificationCode(String verificationId) {
        EmailVerification oldVerification = verificationRepo
            .findByVerificationId(verificationId)
            .orElseThrow(() -> new RuntimeException("Vérification introuvable"));

        // Invalider l'ancienne vérification
        oldVerification.setStatus("EXPIRED");
        verificationRepo.save(oldVerification);

        // Créer une nouvelle vérification
        return sendVerificationCode(oldVerification.getEmail(), oldVerification.getReportId());
    }

    /**
     * Vérifie le statut d'une vérification
     */
    public VerificationStatus checkStatus(String verificationId) {
        EmailVerification verification = verificationRepo
            .findByVerificationId(verificationId)
            .orElseThrow(() -> new RuntimeException("Vérification introuvable"));

        return new VerificationStatus(
            verification.getStatus(),
            MAX_ATTEMPTS - verification.getAttempts()
        );
    }

    /**
     * Génère un code aléatoire à 6 chiffres
     */
    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Nettoie les vérifications expirées (toutes les heures)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredVerifications() {
        verificationRepo.deleteByExpiresAtBeforeAndStatusNot(
            LocalDateTime.now().minusHours(1),
            "VERIFIED"
        );
    }

    // DTO pour le statut
    public record VerificationStatus(String status, int attemptsLeft) {}
}
```

---

## 🌐 4. Controller

### Fichier : `EmailVerificationController.java`

```java
package com.sews.autoreport.controller;

import com.sews.autoreport.model.EmailVerification;
import com.sews.autoreport.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmailVerificationController {

    private final EmailVerificationService verificationService;

    /**
     * Envoie un code de vérification par email
     */
    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerificationCode(@RequestBody SendCodeRequest request) {
        try {
            EmailVerification verification = verificationService
                .sendVerificationCode(request.email(), request.reportId());
            
            return ResponseEntity.ok(Map.of(
                "verificationId", verification.getVerificationId(),
                "expiresAt", verification.getExpiresAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Vérifie le code et envoie le rapport
     */
    @PostMapping("/verify-and-send")
    public ResponseEntity<?> verifyAndSendReport(@RequestBody VerifyCodeRequest request) {
        try {
            verificationService.verifyAndSendReport(
                request.verificationId(), 
                request.code()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Rapport envoyé avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Renvoie un nouveau code
     */
    @PostMapping("/resend-verification/{verificationId}")
    public ResponseEntity<?> resendVerificationCode(@PathVariable String verificationId) {
        try {
            EmailVerification verification = verificationService
                .resendVerificationCode(verificationId);
            
            return ResponseEntity.ok(Map.of(
                "verificationId", verification.getVerificationId(),
                "expiresAt", verification.getExpiresAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Vérifie le statut d'une vérification
     */
    @GetMapping("/verification-status/{verificationId}")
    public ResponseEntity<?> checkVerificationStatus(@PathVariable String verificationId) {
        try {
            var status = verificationService.checkStatus(verificationId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DTOs
    public record SendCodeRequest(String email, Long reportId) {}
    public record VerifyCodeRequest(String verificationId, String code) {}
}
```

---

## 📧 5. Email Service (Extension)

Ajoutez ces méthodes à votre `EmailService.java` existant :

```java
/**
 * Envoie un code de vérification par email
 */
public void sendVerificationCode(String to, String code, String reportTitle) {
    String subject = "Code de vérification - AutoReport";
    
    String body = String.format("""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <h2 style="color: #F59E0B;">Code de vérification</h2>
            <p>Vous avez demandé à recevoir le rapport <strong>%s</strong>.</p>
            <p>Votre code de vérification est :</p>
            <div style="background: #FEF3C7; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px;">
                <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #92400E;">%s</span>
            </div>
            <p><strong>⏱️ Ce code expire dans 5 minutes.</strong></p>
            <p style="color: #6B7280; font-size: 14px;">
                Si vous n'avez pas demandé ce code, ignorez cet email.
            </p>
            <hr style="border: none; border-top: 1px solid #E5E7EB; margin: 30px 0;">
            <p style="color: #9CA3AF; font-size: 12px; text-align: center;">
                AutoReport © 2024 - Système de rapports automatisés
            </p>
        </div>
        """, reportTitle, code);
    
    sendEmail(to, subject, body);
}

/**
 * Envoie un rapport par email après vérification
 */
public void sendReportByEmail(String to, Report report) {
    String subject = "Rapport : " + report.getTitle();
    
    String body = String.format("""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <h2 style="color: #F59E0B;">Votre rapport est prêt !</h2>
            <p>Voici le rapport que vous avez demandé :</p>
            <ul>
                <li><strong>Titre :</strong> %s</li>
                <li><strong>Période :</strong> %s → %s</li>
                <li><strong>Production totale :</strong> %d unités</li>
                <li><strong>Taux de qualité :</strong> %s%%</li>
            </ul>
            <p>Le rapport est disponible en pièce jointe.</p>
            <hr style="border: none; border-top: 1px solid #E5E7EB; margin: 30px 0;">
            <p style="color: #9CA3AF; font-size: 12px; text-align: center;">
                AutoReport © 2024 - Système de rapports automatisés
            </p>
        </div>
        """,
        report.getTitle(),
        report.getPeriodStart(),
        report.getPeriodEnd(),
        report.getTotalProduction(),
        report.getQualityRate()
    );
    
    // TODO: Ajouter le PDF en pièce jointe
    sendEmailWithAttachment(to, subject, body, generateReportPDF(report));
}
```

---

## 🔒 6. Sécurité

### Points importants :

1. **Hash des codes** : Les codes sont stockés hashés avec BCrypt (jamais en clair)
2. **Limitation des tentatives** : Maximum 3 tentatives par code
3. **Expiration** : Codes valides 5 minutes uniquement
4. **Nettoyage automatique** : Suppression des vérifications expirées toutes les heures
5. **UUID unique** : Chaque vérification a un ID unique impossible à deviner

---

## 📊 7. Exemple de Flux Complet

### Request 1 : Demander un code

```http
POST /api/email/send-verification
Content-Type: application/json
Authorization: Bearer <token>

{
  "email": "user@example.com",
  "reportId": 42
}
```

**Response :**
```json
{
  "verificationId": "550e8400-e29b-41d4-a716-446655440000",
  "expiresAt": "2024-01-15T14:35:00"
}
```

### Request 2 : Vérifier le code

```http
POST /api/email/verify-and-send
Content-Type: application/json
Authorization: Bearer <token>

{
  "verificationId": "550e8400-e29b-41d4-a716-446655440000",
  "code": "123456"
}
```

**Response :**
```json
{
  "success": true,
  "message": "Rapport envoyé avec succès"
}
```

---

## ✅ Checklist d'Implémentation Backend

- [ ] Créer l'entité `EmailVerification`
- [ ] Créer le repository `EmailVerificationRepository`
- [ ] Créer le service `EmailVerificationService`
- [ ] Créer le controller `EmailVerificationController`
- [ ] Ajouter les méthodes email dans `EmailService`
- [ ] Configurer le `PasswordEncoder` (BCrypt)
- [ ] Activer `@EnableScheduling` pour le nettoyage automatique
- [ ] Créer la table SQL `email_verifications`
- [ ] Tester l'envoi de codes
- [ ] Tester la vérification
- [ ] Tester les cas d'erreur (code expiré, trop de tentatives, etc.)

---

## 🎯 Avantages de cette Approche

✅ **Sécurité renforcée** : Double vérification avant envoi  
✅ **Protection contre le spam** : Limitation des tentatives  
✅ **Expérience utilisateur** : Processus fluide et guidé  
✅ **Traçabilité** : Historique complet des vérifications  
✅ **Scalabilité** : Nettoyage automatique des données  

---

**Prêt pour l'implémentation ! 🚀**
