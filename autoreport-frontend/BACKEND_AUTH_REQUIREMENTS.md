# Exigences Backend pour l'Authentification Sécurisée

Ce document détaille les changements nécessaires côté backend Spring Boot pour supporter l'authentification sécurisée avec httpOnly cookies et refresh tokens.

## ⚠️ Important

Le frontend a été mis à jour pour utiliser des cookies httpOnly et des refresh tokens. Le backend doit maintenant implémenter les endpoints correspondants.

## Changements Requis Backend

### 1. Configuration CORS

Modifier la configuration CORS pour accepter les credentials (cookies) :

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173") // Frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // IMPORTANT : permet l'envoi de cookies
                .maxAge(3600);
    }
}
```

### 2. Entité RefreshToken

Créer une nouvelle entité pour stocker les refresh tokens :

```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Getters, setters, constructors...
}
```

### 3. Repository RefreshToken

```java
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByToken(String token);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteAllExpired(@Param("now") LocalDateTime now);
}
```

### 4. Service RefreshToken

```java
@Service
@Transactional
public class RefreshTokenService {
    
    @Value("${app.jwt.refresh-expiration-ms:604800000}") // 7 jours
    private Long refreshTokenDurationMs;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found")));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plus(refreshTokenDurationMs, ChronoUnit.MILLIS));
        refreshToken.setCreatedAt(LocalDateTime.now());
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token expired. Please login again.");
        }
        return token;
    }
    
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUser(user);
    }
    
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h du matin
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllExpired(LocalDateTime.now());
    }
}
```

### 5. Modifier AuthController - Endpoint Login

Modifier `/api/auth/login` pour créer un refresh token et l'envoyer en cookie httpOnly :

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    // 1. Authentification (existant)
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    // 2. Générer le JWT access token (existant)
    String accessToken = jwtTokenProvider.generateToken(authentication);
    
    // 3. Créer le refresh token
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
    
    // 4. Envoyer le refresh token dans un cookie httpOnly
    Cookie refreshCookie = new Cookie("refreshToken", refreshToken.getToken());
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(false); // Mettre à true en production avec HTTPS
    refreshCookie.setPath("/api/auth");
    refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 jours
    response.addCookie(refreshCookie);
    
    // 5. Retourner les infos utilisateur + access token
    return ResponseEntity.ok(new LoginResponse(
        accessToken,
        userDetails.getEmail(),
        userDetails.getRole(),
        userDetails.getFullName()
    ));
}
```

### 6. Nouveau Endpoint - Refresh Token

Créer `/api/auth/refresh` pour renouveler l'access token :

```java
@PostMapping("/refresh")
public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                      HttpServletResponse response) {
    if (refreshToken == null || refreshToken.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new MessageResponse("Refresh token manquant"));
    }
    
    try {
        // 1. Vérifier le refresh token
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
            .orElseThrow(() -> new TokenRefreshException("Refresh token invalide"));
        
        // 2. Vérifier l'expiration
        token = refreshTokenService.verifyExpiration(token);
        
        // 3. Générer un nouvel access token
        User user = token.getUser();
        String newAccessToken = jwtTokenProvider.generateTokenFromUser(user);
        
        // 4. (Optionnel) Renouveler aussi le refresh token (rotation)
        refreshTokenService.deleteByToken(refreshToken);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        
        Cookie newRefreshCookie = new Cookie("refreshToken", newRefreshToken.getToken());
        newRefreshCookie.setHttpOnly(true);
        newRefreshCookie.setSecure(false);
        newRefreshCookie.setPath("/api/auth");
        newRefreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(newRefreshCookie);
        
        // 5. Retourner le nouveau access token
        return ResponseEntity.ok(new RefreshResponse(
            newAccessToken,
            user.getEmail(),
            user.getRole(),
            user.getFullName()
        ));
        
    } catch (TokenRefreshException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new MessageResponse(e.getMessage()));
    }
}
```

### 7. Nouveau Endpoint - Logout

Créer `/api/auth/logout` pour invalider le refresh token :

```java
@PostMapping("/logout")
public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                HttpServletResponse response) {
    if (refreshToken != null) {
        refreshTokenService.deleteByToken(refreshToken);
    }
    
    // Supprimer le cookie
    Cookie cookie = new Cookie("refreshToken", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/api/auth");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
    
    return ResponseEntity.ok(new MessageResponse("Déconnexion réussie"));
}
```

### 8. DTOs Nécessaires

```java
// LoginResponse.java (modifier existant)
public class LoginResponse {
    private String token;
    private String email;
    private String role;
    private String fullName;
    // Constructeurs, getters, setters...
}

// RefreshResponse.java (nouveau)
public class RefreshResponse {
    private String token;
    private String email;
    private String role;
    private String fullName;
    // Constructeurs, getters, setters...
}

// TokenRefreshException.java (nouveau)
public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String message) {
        super(message);
    }
}
```

### 9. Configuration application.properties

Ajouter les propriétés suivantes :

```properties
# JWT Configuration
app.jwt.secret=VotreSecretKeyTresLongueEtSecurise
app.jwt.expiration-ms=3600000
app.jwt.refresh-expiration-ms=604800000

# 1 heure pour access token
# 7 jours pour refresh token
```

## Avantages de cette Approche

✅ **Sécurité renforcée**
- Les refresh tokens sont stockés dans des cookies httpOnly (non accessibles en JavaScript)
- Protection contre les attaques XSS
- Les tokens peuvent être révoqués côté serveur

✅ **Expérience utilisateur améliorée**
- Refresh automatique du token sans déconnecter l'utilisateur
- Session persistante pendant 7 jours

✅ **Traçabilité**
- Possibilité d'auditer les refresh tokens actifs
- Déconnexion de tous les appareils possible

## Sécurité Supplémentaire (Production)

Pour la production, considérez :

1. **HTTPS obligatoire** : Mettre `cookie.setSecure(true)`
2. **SameSite** : Ajouter l'attribut `SameSite=Strict` aux cookies
3. **Rotation des refresh tokens** : Changer le refresh token à chaque utilisation
4. **Device tracking** : Lier les refresh tokens à l'appareil/IP
5. **Rate limiting** : Limiter les tentatives de refresh
6. **Token blacklist** : Maintenir une liste des tokens révoqués

## Test de l'Implémentation

Une fois le backend implémenté :

1. Tester le login : vérifier que le cookie `refreshToken` est bien créé
2. Tester le refresh : attendre 30 min et vérifier le renouvellement automatique
3. Tester le logout : vérifier que le cookie est bien supprimé
4. Tester l'expiration : attendre 7 jours et vérifier la déconnexion

## Migration depuis l'Ancien Système

Si vous avez déjà des utilisateurs connectés :

1. Déployer d'abord le backend avec la nouvelle logique
2. Le frontend continuera à fonctionner avec localStorage
3. À la prochaine connexion, les utilisateurs auront le nouveau système
4. Les anciens tokens expireront naturellement

## Support

Pour toute question sur l'implémentation, consultez :
- [Spring Security JWT Guide](https://www.bezkoder.com/spring-boot-jwt-authentication/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
