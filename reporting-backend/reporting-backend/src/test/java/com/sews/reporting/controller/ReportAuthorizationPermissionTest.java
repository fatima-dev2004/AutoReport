package com.sews.reporting.controller;

import com.sews.reporting.entity.*;
import com.sews.reporting.repository.ProductionReportRepository;
import com.sews.reporting.repository.ReportAuthorizationRepository;
import com.sews.reporting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * NOTE IMPORTANTE (voir README section "Tests") :
 * Le fichier ReportControllerTest.java existant utilise des noms de champs et de
 * methodes (setReportName, setStartDate, importProductionData(...), ReportType
 * importe hors de son package...) qui ne correspondent plus aux entites/services
 * actuels (ProductionReport.title, .periodStart, ExcelImportService.importFile(file)).
 * Il ne compilera probablement pas en l'etat -> a corriger avant "mvn test".
 *
 * Ce nouveau fichier couvre specifiquement la matrice d'autorisation d'acces aux
 * rapports demandee au point Priorite 3 :
 *   - Un USER simple ne voit/telecharge/supprime que ce qui lui est autorise.
 *   - Un MANAGER n'accede pas aux fonctions reservees a l'administrateur.
 *   - Un utilisateur desactive ne peut plus s'authentifier.
 * Ecrit contre le code actuel mais N'A PAS PU ETRE COMPILE NI EXECUTE dans cet
 * environnement (pas de Maven / pas de reseau) : a lancer avec `mvn test` avant
 * de considerer cette priorite comme terminee, et a ajuster si necessaire.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportAuthorizationPermissionTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductionReportRepository reportRepository;
    @Autowired
    private ReportAuthorizationRepository reportAuthorizationRepository;

    private User admin;
    private User managerDeptA;
    private User userDeptA;
    private User userDeptB;
    private User disabledUser;
    private ProductionReport reportDeptA;

    @BeforeEach
    void setUp() {
        admin = save(user("admin@sews.com", Role.ADMIN, "IT", true));
        managerDeptA = save(user("manager.a@sews.com", Role.MANAGER, "Production-A", true));
        userDeptA = save(user("user.a@sews.com", Role.USER, "Production-A", true));
        userDeptB = save(user("user.b@sews.com", Role.USER, "Production-B", true));
        disabledUser = save(user("disabled@sews.com", Role.USER, "Production-A", false));

        reportDeptA = reportRepository.save(ProductionReport.builder()
                .title("Rapport production A")
                .type(ProductionReport.ReportType.PRODUCTION)
                .periodStart(LocalDate.now().minusDays(7))
                .periodEnd(LocalDate.now())
                .department("Production-A")
                .generatedBy(managerDeptA)
                .build());

        // Autorisation explicite : userDeptA a acces a reportDeptA, userDeptB non.
        reportAuthorizationRepository.save(ReportAuthorization.builder()
                .report(reportDeptA)
                .authorizedUser(userDeptA)
                .authorizedBy(admin)
                .build());
    }

    private User user(String email, Role role, String dept, boolean enabled) {
        User u = User.builder()
                .email(email)
                .password("{noop}password")
                .fullName(email)
                .role(role)
                .department(dept)
                .enabled(enabled)
                .build();
        u.assignDefaultPermissions();
        return u;
    }

    private User save(User u) {
        return userRepository.save(u);
    }

    // ---- 1) Un USER simple non autorise ne doit pas voir le rapport d'un autre departement ----
    @Test
    @WithMockUser(username = "user.b@sews.com", roles = "USER", authorities = "VIEW_REPORTS")
    void userSansAutorisation_neDoitPasTelechargerLeRapport() throws Exception {
        mockMvc.perform(get("/api/reports/" + reportDeptA.getId() + "/download/pdf"))
                .andExpect(status().isForbidden());
    }

    // ---- 2) Un USER autorise peut telecharger ----
    @Test
    @WithMockUser(username = "user.a@sews.com", roles = "USER", authorities = {"VIEW_REPORTS", "DOWNLOAD_REPORTS"})
    void userAutorise_peutTelechargerLeRapport() throws Exception {
        mockMvc.perform(get("/api/reports/" + reportDeptA.getId() + "/download/pdf"))
                // 200 si le fichier PDF existe reellement sur disque, sinon 404 applicatif :
                // dans les deux cas la verification de permission (403) ne doit pas se declencher.
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertNotEquals(403, status,
                            "Un utilisateur explicitement autorise ne doit jamais recevoir 403");
                });
    }

    // ---- 3) Un USER ne doit pas pouvoir supprimer un rapport, meme autorise en lecture ----
    @Test
    @WithMockUser(username = "user.a@sews.com", roles = "USER", authorities = {"VIEW_REPORTS", "DOWNLOAD_REPORTS"})
    void user_neDoitJamaisPouvoirSupprimerUnRapport() throws Exception {
        mockMvc.perform(delete("/api/reports/" + reportDeptA.getId()))
                .andExpect(status().isForbidden());
    }

    // ---- 4) Un MANAGER ne doit pas acceder aux endpoints reserves ADMIN ----
    @Test
    @WithMockUser(username = "manager.a@sews.com", roles = "MANAGER")
    void manager_neDoitPasAccederAuxParametresAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/settings/smtp"))
                .andExpect(status().isForbidden());
    }

    // ---- 5) L'ADMIN accede aux endpoints d'administration des autorisations ----
    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN")
    void admin_peutConsulterLesAutorisationsDuRapport() throws Exception {
        mockMvc.perform(get("/api/admin/report-authorizations/reports/" + reportDeptA.getId()))
                .andExpect(status().isOk());
    }

    // ---- 6) Sans authentification -> 401 partout sur /api/reports/** ----
    @Test
    void nonAuthentifie_recoit401() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isUnauthorized());
    }

    // Rappel manuel restant a faire (non automatisable simplement via MockMvc car
    // depend du filtre JWT / de la vraie table refresh_tokens) :
    //   - Un utilisateur DESACTIVE qui possede deja un token JWT valide ne doit plus
    //     pouvoir s'en servir des que `enabled=false` (voir CustomUserDetailsService /
    //     JwtAuthFilter : verifier qu'ils rechargent bien `enabled` a chaque requete,
    //     pas seulement au login).
}
