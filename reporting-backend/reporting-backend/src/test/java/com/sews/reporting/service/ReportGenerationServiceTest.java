package com.sews.reporting.service;

import com.sews.reporting.dto.ReportGenerationRequest;
import com.sews.reporting.entity.ProductionRecord;
import com.sews.reporting.entity.ProductionReport;
import com.sews.reporting.entity.Role;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.ProductionRecordRepository;
import com.sews.reporting.repository.ProductionReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ReportGenerationService
 * Couvre : génération de rapports, calcul KPI, création fichiers PDF/Excel
 */
@ExtendWith(MockitoExtension.class)
class ReportGenerationServiceTest {

    @Mock
    private ProductionRecordRepository productionRecordRepository;

    @Mock
    private ProductionReportRepository productionReportRepository;

    @Mock
    private SystemSettingsService systemSettingsService;

    @InjectMocks
    private ReportGenerationService reportGenerationService;

    @TempDir
    Path tempDir;

    private User testUser;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ProductionRecord> testRecords;

    @BeforeEach
    void setUp() {
        // Configurer le répertoire temporaire pour les rapports
        ReflectionTestUtils.setField(reportGenerationService, "reportsDir", tempDir.toString());

        // Créer un utilisateur de test
        testUser = User.builder()
                .id(1L)
                .email("test@sews.com")
                .fullName("Test User")
                .role(Role.MANAGER)
                .department("Production")
                .build();

        // Définir une période de test
        startDate = LocalDate.of(2026, 7, 1);
        endDate = LocalDate.of(2026, 7, 31);

        // Créer des données de production de test
        testRecords = Arrays.asList(
                ProductionRecord.builder()
                        .productionDate(LocalDate.of(2026, 7, 1))
                        .line("Ligne A")
                        .quantity(1000.0)
                        .defects(50)
                        .sourceFileName("test.xlsx")
                        .build(),
                ProductionRecord.builder()
                        .productionDate(LocalDate.of(2026, 7, 2))
                        .line("Ligne A")
                        .quantity(1200.0)
                        .defects(30)
                        .sourceFileName("test.xlsx")
                        .build(),
                ProductionRecord.builder()
                        .productionDate(LocalDate.of(2026, 7, 1))
                        .line("Ligne B")
                        .quantity(800.0)
                        .defects(40)
                        .sourceFileName("test.xlsx")
                        .build()
        );

        // Configurer les settings système
        when(systemSettingsService.getSetting(eq("company.name"), anyString()))
                .thenReturn("SEWS CABIND");
        when(systemSettingsService.getSetting(eq("company.logo"), anyString()))
                .thenReturn("");
    }

    @Test
    void testGenerateProductionReport_WithValidData_ShouldSucceed() {
        // Arrange
        when(productionRecordRepository.findByProductionDateBetween(startDate, endDate))
                .thenReturn(testRecords);
        when(productionReportRepository.save(any(ProductionReport.class)))
                .thenAnswer(invocation -> {
                    ProductionReport report = invocation.getArgument(0);
                    report.setId(1L);
                    return report;
                });

        // Act
        ProductionReport report = reportGenerationService.generateProductionReport(
                startDate, endDate, testUser
        );

        // Assert
        assertNotNull(report);
        assertEquals(ProductionReport.ReportType.PRODUCTION, report.getType());
        assertEquals(startDate, report.getPeriodStart());
        assertEquals(endDate, report.getPeriodEnd());
        assertEquals(3000.0, report.getTotalProduction()); // 1000 + 1200 + 800
        assertEquals(120, report.getTotalDefects()); // 50 + 30 + 40
        
        // Taux de qualité : (3000 - 120) / 3000 * 100 = 96%
        assertEquals(96.0, report.getQualityRate(), 0.01);
        
        assertNotNull(report.getProductionByLine());
        assertTrue(report.getProductionByLine().contains("Ligne A"));
        assertTrue(report.getProductionByLine().contains("Ligne B"));
        
        assertNotNull(report.getPdfFilePath());
        assertNotNull(report.getExcelFilePath());
        assertEquals(testUser, report.getGeneratedBy());
        assertEquals("Production", report.getDepartment());

        // Vérifier que la sauvegarde a été appelée
        verify(productionReportRepository, times(1)).save(any(ProductionReport.class));
    }

    @Test
    void testGenerateEnhancedReport_WithRequest_ShouldSucceed() {
        // Arrange
        ReportGenerationRequest request = new ReportGenerationRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setReportTitle("Rapport Mensuel Juillet 2026");
        request.setIncludeCoverPage(true);
        request.setGeneratedBy("Test Manager");

        when(productionRecordRepository.findByProductionDateBetween(startDate, endDate))
                .thenReturn(testRecords);
        when(productionReportRepository.save(any(ProductionReport.class)))
                .thenAnswer(invocation -> {
                    ProductionReport report = invocation.getArgument(0);
                    report.setId(2L);
                    return report;
                });

        // Act
        ProductionReport report = reportGenerationService.generateEnhancedReport(request, testUser);

        // Assert
        assertNotNull(report);
        assertEquals("Rapport Mensuel Juillet 2026", report.getTitle());
        assertEquals(3000.0, report.getTotalProduction());
        assertEquals(120, report.getTotalDefects());
        
        verify(productionReportRepository, times(1)).save(any(ProductionReport.class));
    }

    @Test
    void testGenerateReport_WithNoData_ShouldCreateEmptyReport() {
        // Arrange
        when(productionRecordRepository.findByProductionDateBetween(startDate, endDate))
                .thenReturn(List.of());
        when(productionReportRepository.save(any(ProductionReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductionReport report = reportGenerationService.generateProductionReport(
                startDate, endDate, testUser
        );

        // Assert
        assertNotNull(report);
        assertEquals(0.0, report.getTotalProduction());
        assertEquals(0, report.getTotalDefects());
        assertEquals(0.0, report.getQualityRate());
    }

    @Test
    void testGenerateReport_QualityRateCalculation() {
        // Arrange - 100% de qualité (aucun défaut)
        List<ProductionRecord> perfectRecords = Arrays.asList(
                ProductionRecord.builder()
                        .productionDate(startDate)
                        .line("Ligne A")
                        .quantity(1000.0)
                        .defects(0)
                        .build()
        );

        when(productionRecordRepository.findByProductionDateBetween(startDate, endDate))
                .thenReturn(perfectRecords);
        when(productionReportRepository.save(any(ProductionReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductionReport report = reportGenerationService.generateProductionReport(
                startDate, endDate, testUser
        );

        // Assert
        assertEquals(100.0, report.getQualityRate());
    }

    @Test
    void testGenerateReport_MultipleLines_ShouldAggregateCorrectly() {
        // Arrange
        when(productionRecordRepository.findByProductionDateBetween(startDate, endDate))
                .thenReturn(testRecords);
        when(productionReportRepository.save(any(ProductionReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductionReport report = reportGenerationService.generateProductionReport(
                startDate, endDate, testUser
        );

        // Assert
        String productionByLine = report.getProductionByLine();
        assertNotNull(productionByLine);
        
        // Vérifier que les deux lignes sont présentes
        assertTrue(productionByLine.contains("Ligne A"));
        assertTrue(productionByLine.contains("Ligne B"));
        
        // Ligne A devrait avoir 2200 (1000 + 1200)
        // Ligne B devrait avoir 800
        assertTrue(productionByLine.contains("2200") || productionByLine.contains("2200.0"));
        assertTrue(productionByLine.contains("800") || productionByLine.contains("800.0"));
    }

    @Test
    void testGenerateReport_FilePathsAreSet() {
        // Arrange
        when(productionRecordRepository.findByProductionDateBetween(startDate, endDate))
                .thenReturn(testRecords);
        when(productionReportRepository.save(any(ProductionReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductionReport report = reportGenerationService.generateProductionReport(
                startDate, endDate, testUser
        );

        // Assert
        assertNotNull(report.getPdfFilePath());
        assertNotNull(report.getExcelFilePath());
        assertTrue(report.getPdfFilePath().contains("production_"));
        assertTrue(report.getExcelFilePath().contains("production_"));
        assertTrue(report.getPdfFilePath().endsWith(".pdf"));
        assertTrue(report.getExcelFilePath().endsWith(".xlsx"));
    }
}
