package com.sews.reporting.service;

import com.sews.reporting.entity.ProductionRecord;
import com.sews.reporting.exception.InvalidExcelFileException;
import com.sews.reporting.repository.ProductionRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ExcelImportService
 * Couvre : validation fichiers, parsing Excel, gestion erreurs, calcul KPI
 */
@ExtendWith(MockitoExtension.class)
class ExcelImportServiceTest {

    @Mock
    private ProductionRecordRepository productionRecordRepository;

    @InjectMocks
    private ExcelImportService excelImportService;

    private MockMultipartFile validExcelFile;
    private MockMultipartFile invalidFile;

    @BeforeEach
    void setUp() throws Exception {
        // Charger un fichier Excel de test valide depuis les resources
        InputStream validStream = getClass().getClassLoader()
                .getResourceAsStream("test-data/production_valid.xlsx");
        
        if (validStream != null) {
            validExcelFile = new MockMultipartFile(
                    "file",
                    "production_valid.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    validStream
            );
        }

        // Fichier invalide (pas Excel)
        invalidFile = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "This is not an Excel file".getBytes()
        );
    }

    @Test
    void testImportValidFile_ShouldSucceed() {
        // Arrange
        when(productionRecordRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        if (validExcelFile != null) {
            ExcelImportService.ImportResult result = excelImportService.importFile(validExcelFile);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getRecordsImported() > 0);
            assertEquals("production_valid.xlsx", result.getFilename());
            assertNotNull(result.getKpi());
            
            // Vérifier que les données ont été sauvegardées
            verify(productionRecordRepository, times(1)).saveAll(anyList());
        }
    }

    @Test
    void testImportInvalidFileFormat_ShouldThrowException() {
        // Act & Assert
        InvalidExcelFileException exception = assertThrows(
                InvalidExcelFileException.class,
                () -> excelImportService.importFile(invalidFile)
        );

        assertTrue(exception.getMessage().contains("seul .xlsx est accepte"));
        
        // Vérifier qu'aucune sauvegarde n'a été tentée
        verify(productionRecordRepository, never()).saveAll(anyList());
    }

    @Test
    void testImportNullFile_ShouldThrowException() {
        // Arrange
        MockMultipartFile nullNameFile = new MockMultipartFile(
                "file",
                null,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        // Act & Assert
        assertThrows(
                InvalidExcelFileException.class,
                () -> excelImportService.importFile(nullNameFile)
        );
    }

    @Test
    void testImportEmptyFile_ShouldHandleGracefully() throws Exception {
        // Arrange
        InputStream emptyStream = getClass().getClassLoader()
                .getResourceAsStream("test-data/production_empty.xlsx");
        
        if (emptyStream != null) {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "production_empty.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    emptyStream
            );

            // Act
            ExcelImportService.ImportResult result = excelImportService.importFile(emptyFile);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getRecordsImported());
            
            // Aucune sauvegarde pour fichier vide
            verify(productionRecordRepository, never()).saveAll(anyList());
        }
    }

    @Test
    void testImportFileWithErrors_ShouldReportErrors() throws Exception {
        // Arrange
        InputStream errorStream = getClass().getClassLoader()
                .getResourceAsStream("test-data/production_with_errors.xlsx");
        
        if (errorStream != null) {
            MockMultipartFile errorFile = new MockMultipartFile(
                    "file",
                    "production_with_errors.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    errorStream
            );

            when(productionRecordRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ExcelImportService.ImportResult result = excelImportService.importFile(errorFile);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getErrors().size() > 0, "Devrait contenir des erreurs");
            assertTrue(result.getRowsSkipped() > 0, "Devrait avoir des lignes ignorées");
        }
    }

    @Test
    void testImportResult_KPICalculation() throws Exception {
        // Arrange
        when(productionRecordRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        if (validExcelFile != null) {
            // Act
            ExcelImportService.ImportResult result = excelImportService.importFile(validExcelFile);

            // Assert
            ExcelImportService.ImportKPI kpi = result.getKpi();
            assertNotNull(kpi);
            assertTrue(kpi.totalProduction() >= 0);
            assertTrue(kpi.totalDefects() >= 0);
            assertTrue(kpi.qualityRate() >= 0 && kpi.qualityRate() <= 100);
            assertTrue(kpi.defectRate() >= 0 && kpi.defectRate() <= 100);
            assertNotNull(kpi.periodStart());
            assertNotNull(kpi.periodEnd());
        }
    }

    @Test
    void testImportMissingColumns_ShouldThrowException() throws Exception {
        // Arrange
        InputStream missingColStream = getClass().getClassLoader()
                .getResourceAsStream("test-data/production_missing_columns.xlsx");
        
        if (missingColStream != null) {
            MockMultipartFile missingColFile = new MockMultipartFile(
                    "file",
                    "production_missing_columns.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    missingColStream
            );

            // Act & Assert
            InvalidExcelFileException exception = assertThrows(
                    InvalidExcelFileException.class,
                    () -> excelImportService.importFile(missingColFile)
            );

            assertTrue(exception.getMessage().contains("Colonnes manquantes"));
        }
    }
}
