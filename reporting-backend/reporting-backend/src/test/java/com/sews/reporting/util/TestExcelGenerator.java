package com.sews.reporting.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

/**
 * Utilitaire pour générer les fichiers Excel de test
 * Exécuter cette classe en tant que main() pour créer les fichiers de test
 */
public class TestExcelGenerator {

    private static final String TEST_DATA_DIR = "src/test/resources/test-data";

    public static void main(String[] args) {
        try {
            // Créer le répertoire s'il n'existe pas
            Path testDataPath = Paths.get(TEST_DATA_DIR);
            Files.createDirectories(testDataPath);

            generateValidFile();
            generateEmptyFile();
            generateFileWithErrors();
            generateFileMissingColumns();

            System.out.println("✅ Tous les fichiers Excel de test ont été générés avec succès dans : " + TEST_DATA_DIR);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération des fichiers de test : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Génère un fichier Excel valide avec des données correctes
     */
    private static void generateValidFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Production");

            // Créer le style pour les en-têtes
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // En-têtes
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "Date", headerStyle);
            createCell(headerRow, 1, "Ligne", headerStyle);
            createCell(headerRow, 2, "Quantité", headerStyle);
            createCell(headerRow, 3, "Défauts", headerStyle);

            // Style pour les dates
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

            // Données de test
            Object[][] data = {
                    {LocalDate.of(2026, 7, 1), "Ligne A", 1000.0, 50},
                    {LocalDate.of(2026, 7, 1), "Ligne B", 800.0, 40},
                    {LocalDate.of(2026, 7, 2), "Ligne A", 1200.0, 30},
                    {LocalDate.of(2026, 7, 2), "Ligne B", 850.0, 35},
                    {LocalDate.of(2026, 7, 3), "Ligne A", 1100.0, 45},
                    {LocalDate.of(2026, 7, 3), "Ligne B", 900.0, 25},
                    {LocalDate.of(2026, 7, 4), "Ligne A", 1050.0, 55},
                    {LocalDate.of(2026, 7, 4), "Ligne B", 820.0, 30}
            };

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                
                // Date
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue((LocalDate) data[i][0]);
                dateCell.setCellStyle(dateStyle);
                
                // Ligne
                row.createCell(1).setCellValue((String) data[i][1]);
                
                // Quantité
                row.createCell(2).setCellValue((Double) data[i][2]);
                
                // Défauts
                row.createCell(3).setCellValue((Integer) data[i][3]);
            }

            // Auto-size columns
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // Écrire le fichier
            try (FileOutputStream fileOut = new FileOutputStream(TEST_DATA_DIR + "/production_valid.xlsx")) {
                workbook.write(fileOut);
            }
            System.out.println("✓ production_valid.xlsx créé");
        }
    }

    /**
     * Génère un fichier Excel vide (seulement les en-têtes)
     */
    private static void generateEmptyFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Production");

            // Seulement les en-têtes
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Ligne");
            headerRow.createCell(2).setCellValue("Quantité");
            headerRow.createCell(3).setCellValue("Défauts");

            try (FileOutputStream fileOut = new FileOutputStream(TEST_DATA_DIR + "/production_empty.xlsx")) {
                workbook.write(fileOut);
            }
            System.out.println("✓ production_empty.xlsx créé");
        }
    }

    /**
     * Génère un fichier avec quelques lignes d'erreurs (dates invalides, valeurs négatives)
     */
    private static void generateFileWithErrors() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Production");

            // En-têtes
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Ligne");
            headerRow.createCell(2).setCellValue("Quantité");
            headerRow.createCell(3).setCellValue("Défauts");

            // Ligne 1 - Valide
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("2026-07-01");
            row1.createCell(1).setCellValue("Ligne A");
            row1.createCell(2).setCellValue(1000.0);
            row1.createCell(3).setCellValue(50);

            // Ligne 2 - Date invalide
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("date invalide");
            row2.createCell(1).setCellValue("Ligne B");
            row2.createCell(2).setCellValue(800.0);
            row2.createCell(3).setCellValue(40);

            // Ligne 3 - Valide
            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("2026-07-02");
            row3.createCell(1).setCellValue("Ligne A");
            row3.createCell(2).setCellValue(1200.0);
            row3.createCell(3).setCellValue(30);

            // Ligne 4 - Quantité négative
            Row row4 = sheet.createRow(4);
            row4.createCell(0).setCellValue("2026-07-03");
            row4.createCell(1).setCellValue("Ligne B");
            row4.createCell(2).setCellValue(-500.0);
            row4.createCell(3).setCellValue(20);

            // Ligne 5 - Ligne vide
            Row row5 = sheet.createRow(5);
            row5.createCell(0).setCellValue("2026-07-04");
            row5.createCell(1).setCellValue("");
            row5.createCell(2).setCellValue(900.0);
            row5.createCell(3).setCellValue(35);

            // Ligne 6 - Valide
            Row row6 = sheet.createRow(6);
            row6.createCell(0).setCellValue("2026-07-05");
            row6.createCell(1).setCellValue("Ligne C");
            row6.createCell(2).setCellValue(1100.0);
            row6.createCell(3).setCellValue(25);

            try (FileOutputStream fileOut = new FileOutputStream(TEST_DATA_DIR + "/production_with_errors.xlsx")) {
                workbook.write(fileOut);
            }
            System.out.println("✓ production_with_errors.xlsx créé");
        }
    }

    /**
     * Génère un fichier avec des colonnes manquantes
     */
    private static void generateFileMissingColumns() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Production");

            // En-têtes incomplets (manque "Défauts")
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Ligne");
            headerRow.createCell(2).setCellValue("Quantité");
            // Pas de colonne "Défauts"

            // Quelques lignes de données
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("2026-07-01");
            row1.createCell(1).setCellValue("Ligne A");
            row1.createCell(2).setCellValue(1000.0);

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("2026-07-02");
            row2.createCell(1).setCellValue("Ligne B");
            row2.createCell(2).setCellValue(800.0);

            try (FileOutputStream fileOut = new FileOutputStream(TEST_DATA_DIR + "/production_missing_columns.xlsx")) {
                workbook.write(fileOut);
            }
            System.out.println("✓ production_missing_columns.xlsx créé");
        }
    }

    private static void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
