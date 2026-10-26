package com.sews.reporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sews.reporting.service.ExcelImportService;
import com.sews.reporting.dto.ReportSummaryDto;
import com.sews.reporting.entity.*;
import com.sews.reporting.repository.ProductionReportRepository;
import com.sews.reporting.repository.UserRepository;
import com.sews.reporting.service.ReportGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour ReportController - endpoints de gestion des rapports.
 *
 * NOTE : ce fichier a ete corrige pour correspondre a la vraie forme de
 * l'API (ProductionReport / ReportGenerationService / ExcelImportService
 * tels qu'ils existent reellement dans le projet), le fichier original
 * ayant ete ecrit contre une API imaginaire qui ne compilait pas.
 *
 * Points importants sur le controleur reel (ReportController.java) :
 *  - /generate prend des @RequestParam "start" / "end" (pas "startDate"/
 *    "endDate"/"reportType"), et appelle
 *    reportGenerationService.generateProductionReport(start, end, user).
 *  - Il n'y a PAS de ReportAuthorizationService injecte dans le controleur.
 *    L'autorisation de telechargement pour un USER passe par une vraie
 *    requete SQL : productionReportRepository.canUserAccessReport(...),
 *    qui autorise si l'utilisateur est le createur, s'il est du meme
 *    departement que le rapport, ou s'il a une autorisation explicite.
 *    On teste donc ca avec de vraies donnees (department different),
 *    pas avec un mock.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportGenerationService reportGenerationService;

    @MockBean
    private ExcelImportService excelImportService;

    @Autowired
    private ProductionReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User managerUser;
    private User regularUser;       // meme departement que testReport -> a acces
    private User otherDeptUser;     // departement different -> pas d'acces
    private ProductionReport testReport;

    @BeforeEach
    void setUp() {
        adminUser = createUser("admin@sews.com", Role.ADMIN, "IT");
        managerUser = createUser("manager@sews.com", Role.MANAGER, "Production");
        regularUser = createUser("user@sews.com", Role.USER, "Production");
        otherDeptUser = createUser("otheruser@sews.com", Role.USER, "Logistique");

        testReport = new ProductionReport();
        testReport.setTitle("Test Report");
        testReport.setType(ProductionReport.ReportType.PRODUCTION);
        testReport.setPeriodStart(LocalDate.now().minusDays(7));
        testReport.setPeriodEnd(LocalDate.now());
        testReport.setGeneratedAt(LocalDateTime.now());
        testReport.setGeneratedBy(managerUser);
        testReport.setDepartment("Production");
        testReport.setPdfFilePath("/reports/test.pdf");
        testReport.setExcelFilePath("/reports/test.xlsx");
        testReport = reportRepository.save(testReport);
    }

    private User createUser(String email, Role role, String department) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        user.setDepartment(department);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    // ========== Tests GET /api/reports ==========

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN")
    void getReports_AsAdmin_ReturnsAllReports() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title").exists());
    }

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER")
    void getReports_AsManager_ReturnsManagerReports() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(List.class)));
    }

    @Test
    @WithMockUser(username = "user@sews.com", roles = "USER")
    void getReports_AsUser_ReturnsAuthorizedReportsOnly() throws Exception {
        // regularUser est dans le meme departement ("Production") que testReport,
        // donc la requete d'autorisation reelle doit le laisser passer.
        mockMvc.perform(get("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(List.class)));
    }

    @Test
    void getReports_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ========== Tests POST /api/reports/import ==========

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER", authorities = "IMPORT_EXCEL")
    void importExcel_WithValidFile_ReturnsSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "production.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test excel content".getBytes()
        );

        ExcelImportService.ImportResult mockResult = ExcelImportService.ImportResult.builder()
                .success(true)
                .recordsImported(10)
                .errors(List.of())
                .build();

        when(excelImportService.importFile(any())).thenReturn(mockResult);

        mockMvc.perform(multipart("/api/reports/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.recordsImported").value(10));
    }

    @Test
    @WithMockUser(username = "user@sews.com", roles = "USER")
    void importExcel_AsUser_Returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "production.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test".getBytes()
        );

        mockMvc.perform(multipart("/api/reports/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER", authorities = "IMPORT_EXCEL")
    void importExcel_WithInvalidFile_ReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "not an excel file".getBytes()
        );

        when(excelImportService.importFile(any()))
                .thenThrow(new IllegalArgumentException("Invalid file format"));

        mockMvc.perform(multipart("/api/reports/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ========== Tests POST /api/reports/generate ==========

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER", authorities = "GENERATE_REPORTS")
    void generateReport_WithValidParams_ReturnsReport() throws Exception {
        // Le controleur appelle generateProductionReport(start, end, user) et
        // renvoie une entite ProductionReport, convertie en DTO par toDto().
        ProductionReport mockReport = ProductionReport.builder()
                .id(1L)
                .title("Generated Report")
                .type(ProductionReport.ReportType.PRODUCTION)
                .periodStart(LocalDate.of(2026, 7, 1))
                .periodEnd(LocalDate.of(2026, 7, 31))
                .generatedAt(LocalDateTime.now())
                .build();

        when(reportGenerationService.generateProductionReport(any(), any(), any()))
                .thenReturn(mockReport);

        mockMvc.perform(post("/api/reports/generate")
                        .param("start", "2026-07-01")
                        .param("end", "2026-07-31")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Generated Report"))
                .andExpect(jsonPath("$.type").value("PRODUCTION"));
    }

    @Test
    @WithMockUser(username = "user@sews.com", roles = "USER")
    void generateReport_AsUser_Returns403() throws Exception {
        mockMvc.perform(post("/api/reports/generate")
                        .param("start", "2026-07-01")
                        .param("end", "2026-07-31")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== Tests DELETE /api/reports/{id} ==========

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN")
    void deleteReport_AsAdmin_ReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/reports/" + testReport.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER")
    void deleteReport_AsManager_OwnReport_ReturnsSuccess() throws Exception {
        // Le rapport a ete cree par managerUser dans setUp()
        mockMvc.perform(delete("/api/reports/" + testReport.getId())
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@sews.com", roles = "USER")
    void deleteReport_AsUser_Returns403() throws Exception {
        mockMvc.perform(delete("/api/reports/" + testReport.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN")
    void deleteReport_NonExistentReport_Returns404() throws Exception {
        mockMvc.perform(delete("/api/reports/99999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ========== Tests GET /api/reports/{id}/download/pdf ==========

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN", authorities = "DOWNLOAD_REPORTS")
    void downloadPdf_AsAdmin_ReturnsFile() throws Exception {
        mockMvc.perform(get("/api/reports/" + testReport.getId() + "/download/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @WithMockUser(username = "otheruser@sews.com", roles = "USER", authorities = "DOWNLOAD_REPORTS")
    void downloadPdf_AsUser_WithoutAccess_Returns403() throws Exception {
        // otherDeptUser est dans un departement different ("Logistique") de
        // celui du rapport ("Production"), et n'en est pas le createur :
        // l'acces doit etre refuse par la vraie requete SQL du repository.
        mockMvc.perform(get("/api/reports/" + testReport.getId() + "/download/pdf"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN", authorities = "DOWNLOAD_REPORTS")
    void downloadPdf_NonExistentReport_Returns404() throws Exception {
        mockMvc.perform(get("/api/reports/99999/download/pdf"))
                .andExpect(status().isNotFound());
    }

    // ========== Tests GET /api/reports/{id}/download/excel ==========

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER", authorities = "DOWNLOAD_REPORTS")
    void downloadExcel_AsManager_ReturnsFile() throws Exception {
        mockMvc.perform(get("/api/reports/" + testReport.getId() + "/download/excel"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @WithMockUser(username = "otheruser@sews.com", roles = "USER", authorities = "DOWNLOAD_REPORTS")
    void downloadExcel_WithoutPermission_Returns403() throws Exception {
        mockMvc.perform(get("/api/reports/" + testReport.getId() + "/download/excel"))
                .andExpect(status().isForbidden());
    }
}
