package com.sews.reporting.service;

import com.sews.reporting.dto.ChartDataDto;
import com.sews.reporting.dto.ReportGenerationRequest;
import com.sews.reporting.entity.ProductionRecord;
import com.sews.reporting.entity.ProductionReport;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.ProductionRecordRepository;
import com.sews.reporting.repository.ProductionReportRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Section 5.3 (Analyse et traitement des donnees) + 5.4 (Generation automatique des rapports)
 */
@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final ProductionRecordRepository productionRecordRepository;
    private final ProductionReportRepository productionReportRepository;
    private final SystemSettingsService systemSettingsService;
    private final MessageSource messageSource;

    @Value("${app.reports.dir}")
    private String reportsDir;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Génération de rapport AMÉLIORÉE avec page de garde et graphiques
     */
    public ProductionReport generateEnhancedReport(ReportGenerationRequest request, User requestedBy) {
        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();
        
        List<ProductionRecord> records = productionRecordRepository.findByProductionDateBetween(start, end);

        // Calcul des indicateurs
        double totalProduction = records.stream().mapToDouble(ProductionRecord::getQuantity).sum();
        int totalDefects = records.stream().mapToInt(ProductionRecord::getDefects).sum();
        double qualityRate = totalProduction > 0
                ? Math.round(((totalProduction - totalDefects) / totalProduction) * 10000.0) / 100.0
                : 0.0;
        double defectRate = totalProduction > 0
                ? Math.round((totalDefects / totalProduction) * 10000.0) / 100.0
                : 0.0;

        Map<String, Double> productionByLine = records.stream()
                .collect(Collectors.groupingBy(ProductionRecord::getLine,
                        LinkedHashMap::new, Collectors.summingDouble(ProductionRecord::getQuantity)));

        Map<String, Integer> defectsByLine = records.stream()
                .collect(Collectors.groupingBy(ProductionRecord::getLine,
                        LinkedHashMap::new, Collectors.summingInt(ProductionRecord::getDefects)));

        String productionByLineStr = productionByLine.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue().intValue())
                .collect(Collectors.joining(";"));

        String title = request.getReportTitle() != null 
                ? request.getReportTitle() 
                : "Rapport Production - " + end.format(DATE_FMT);

        try {
            Files.createDirectories(Path.of(reportsDir));
        } catch (Exception ignored) {}

        String baseFileName = "production_" + start + "_" + end + "_" + System.currentTimeMillis();

        // Utiliser le template amélioré si demandé
        boolean useEnhanced = request.getIncludeCoverPage() != null && request.getIncludeCoverPage();
        
        String pdfPath = useEnhanced
                ? generateEnhancedPdf(request, title, start, end, totalProduction, totalDefects, 
                        qualityRate, defectRate, productionByLine, defectsByLine, baseFileName, requestedBy)
                : generatePdf(title, start, end, totalProduction, totalDefects, qualityRate, 
                        productionByLine, defectsByLine, baseFileName);

        String excelPath = generateExcel(title, start, end, totalProduction, totalDefects, qualityRate,
                productionByLine, defectsByLine, baseFileName, Locale.FRENCH);

        ProductionReport report = ProductionReport.builder()
                .title(title)
                .type(ProductionReport.ReportType.PRODUCTION)
                .periodStart(start)
                .periodEnd(end)
                .totalProduction(totalProduction)
                .totalDefects(totalDefects)
                .qualityRate(qualityRate)
                .productionByLine(productionByLineStr)
                .pdfFilePath(pdfPath)
                .excelFilePath(excelPath)
                .generatedBy(requestedBy)
                .department(requestedBy != null ? requestedBy.getDepartment() : null)  // Assigner le département
                .build();

        return productionReportRepository.save(report);
    }
    
    /**
     * Génération PDF améliorée avec page de garde, graphiques, et personnalisation
     */
    private String generateEnhancedPdf(ReportGenerationRequest request, String title, LocalDate start, 
                                       LocalDate end, double totalProduction, int totalDefects, 
                                       double qualityRate, double defectRate, Map<String, Double> byLine,
                                       Map<String, Integer> defectsByLine, String baseFileName, User user) {
        try {
            ClassPathResource resource = new ClassPathResource("reports/production_report_enhanced.jrxml");
            try (InputStream jrxmlStream = resource.getInputStream()) {
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                // Préparation des données pour le graphique
                List<ChartDataDto> chartData = new ArrayList<>();
                for (String line : byLine.keySet()) {
                    chartData.add(ChartDataDto.builder()
                            .lineName(line)
                            .quantity(byLine.get(line))
                            .defects(defectsByLine.getOrDefault(line, 0))
                            .build());
                }
                
                // Texte détaillé par ligne
                StringBuilder linesText = new StringBuilder();
                for (String line : byLine.keySet()) {
                    double lineProduction = byLine.get(line);
                    int lineDefects = defectsByLine.getOrDefault(line, 0);
                    double lineQuality = lineProduction > 0 
                            ? Math.round(((lineProduction - lineDefects) / lineProduction) * 10000.0) / 100.0 
                            : 0.0;
                    
                    linesText.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    linesText.append("Ligne ").append(line).append("\n");
                    linesText.append("  • Production : ").append((int)lineProduction).append(" unités\n");
                    linesText.append("  • Défauts    : ").append(lineDefects).append(" unités\n");
                    linesText.append("  • Qualité    : ").append(lineQuality).append(" %\n");
                }
                if (linesText.length() == 0) {
                    linesText.append("Aucune donnée disponible pour cette période.");
                }

                // Récupération des paramètres système
                String companyName = systemSettingsService.getSetting("company.name", "SEWS CABIND");
                String companyLogo = systemSettingsService.getSetting("company.logo", "");
                
                // Paramètres du rapport
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("REPORT_TITLE", title);
                params.put("PERIOD_LABEL", start.format(DATE_FMT) + " au " + end.format(DATE_FMT));
                params.put("TOTAL_PRODUCTION", String.valueOf((int) totalProduction));
                params.put("TOTAL_DEFECTS", String.valueOf(totalDefects));
                params.put("QUALITY_RATE", String.valueOf(qualityRate));
                params.put("DEFECT_RATE", String.valueOf(defectRate));
                params.put("LINES_TEXT", linesText.toString());
                params.put("GENERATED_AT", java.time.LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
                params.put("COMPANY_NAME", companyName);
                params.put("COMPANY_LOGO_PATH", companyLogo.isEmpty() ? null : companyLogo);
                params.put("GENERATED_BY", request.getGeneratedBy() != null 
                        ? request.getGeneratedBy() 
                        : user.getFullName());
                
                // Données pour les graphiques
                params.put("CHART_PRODUCTION_BY_LINE", new JRBeanCollectionDataSource(chartData));
                params.put("CHART_DAILY_PRODUCTION", new JRBeanCollectionDataSource(new ArrayList<>())); // À implémenter si besoin

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

                String pdfPath = reportsDir + "/" + baseFileName + ".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath);
                return pdfPath;
            }
        } catch (Exception e) {
            // Fallback sur le template simple en cas d'erreur
            System.err.println("Erreur avec le template amélioré, utilisation du template simple : " + e.getMessage());
            return generatePdf(title, start, end, totalProduction, totalDefects, qualityRate, byLine, defectsByLine, baseFileName);
        }
    }

    public ProductionReport generateProductionReport(LocalDate start, LocalDate end, User requestedBy) {
        List<ProductionRecord> records = productionRecordRepository.findByProductionDateBetween(start, end);

        // --- Indicateurs (section 5.3) ---
        double totalProduction = records.stream().mapToDouble(ProductionRecord::getQuantity).sum();
        int totalDefects = records.stream().mapToInt(ProductionRecord::getDefects).sum();
        double qualityRate = totalProduction > 0
                ? Math.round(((totalProduction - totalDefects) / totalProduction) * 10000.0) / 100.0
                : 0.0;

        Map<String, Double> productionByLine = records.stream()
                .collect(Collectors.groupingBy(ProductionRecord::getLine,
                        LinkedHashMap::new, Collectors.summingDouble(ProductionRecord::getQuantity)));

        Map<String, Integer> defectsByLine = records.stream()
                .collect(Collectors.groupingBy(ProductionRecord::getLine,
                        LinkedHashMap::new, Collectors.summingInt(ProductionRecord::getDefects)));

        String productionByLineStr = productionByLine.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue().intValue())
                .collect(Collectors.joining(";"));

        String title = "Rapport Production - " + end.format(DATE_FMT);

        try {
            Files.createDirectories(Path.of(reportsDir));
        } catch (Exception ignored) {}

        String baseFileName = "production_" + start + "_" + end + "_" + System.currentTimeMillis();

        String pdfPath = generatePdf(title, start, end, totalProduction, totalDefects, qualityRate,
                productionByLine, defectsByLine, baseFileName);

        String excelPath = generateExcel(title, start, end, totalProduction, totalDefects, qualityRate,
                productionByLine, defectsByLine, baseFileName, Locale.FRENCH);

        ProductionReport report = ProductionReport.builder()
                .title(title)
                .type(ProductionReport.ReportType.PRODUCTION)
                .periodStart(start)
                .periodEnd(end)
                .totalProduction(totalProduction)
                .totalDefects(totalDefects)
                .qualityRate(qualityRate)
                .productionByLine(productionByLineStr)
                .pdfFilePath(pdfPath)
                .excelFilePath(excelPath)
                .generatedBy(requestedBy)
                .department(requestedBy != null ? requestedBy.getDepartment() : null)  // Assigner le département
                .build();

        return productionReportRepository.save(report);
    }

    private String generatePdf(String title, LocalDate start, LocalDate end, double totalProduction,
                                int totalDefects, double qualityRate, Map<String, Double> byLine,
                                Map<String, Integer> defectsByLine, String baseFileName) {
        try {
            ClassPathResource resource = new ClassPathResource("reports/production_report.jrxml");
            try (InputStream jrxmlStream = resource.getInputStream()) {
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                StringBuilder linesText = new StringBuilder();
                for (String line : byLine.keySet()) {
                    linesText.append(line)
                            .append(" : ").append(byLine.get(line).intValue()).append(" unites, ")
                            .append(defectsByLine.getOrDefault(line, 0)).append(" defauts\n");
                }
                if (linesText.isEmpty()) linesText.append("Aucune donnee pour cette periode.");

                Map<String, Object> params = new LinkedHashMap<>();
                params.put("REPORT_TITLE", title);
                params.put("PERIOD_LABEL", start.format(DATE_FMT) + " - " + end.format(DATE_FMT));
                params.put("TOTAL_PRODUCTION", String.valueOf((int) totalProduction));
                params.put("TOTAL_DEFECTS", String.valueOf(totalDefects));
                params.put("QUALITY_RATE", String.valueOf(qualityRate));
                params.put("LINES_TEXT", linesText.toString());
                params.put("GENERATED_AT", java.time.LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

                String pdfPath = reportsDir + "/" + baseFileName + ".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath);
                return pdfPath;
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la generation du PDF : " + e.getMessage(), e);
        }
    }

    private String generateExcel(String title, LocalDate start, LocalDate end, double totalProduction,
                                  int totalDefects, double qualityRate, Map<String, Double> byLine,
                                  Map<String, Integer> defectsByLine, String baseFileName, Locale locale) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(messageSource.getMessage("excel.sheet.production", null, locale));

            // ========== STYLES PROFESSIONNELS ==========
            
            // Style Titre Principal (Bleu foncé, Gras, Blanc, Centré)
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 18);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setFontName("Calibri");
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // Style Sous-titre (Gris clair, Italique)
            CellStyle subtitleStyle = workbook.createCellStyle();
            Font subtitleFont = workbook.createFont();
            subtitleFont.setItalic(true);
            subtitleFont.setFontHeightInPoints((short) 11);
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            subtitleStyle.setFont(subtitleFont);
            subtitleStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Style En-tête Section (Orange, Gras)
            CellStyle sectionHeaderStyle = workbook.createCellStyle();
            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionFont.setFontHeightInPoints((short) 12);
            sectionFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            sectionHeaderStyle.setFont(sectionFont);
            sectionHeaderStyle.setBorderBottom(BorderStyle.MEDIUM);
            sectionHeaderStyle.setBottomBorderColor(IndexedColors.DARK_BLUE.getIndex());
            
            // Style KPI Label
            CellStyle kpiLabelStyle = workbook.createCellStyle();
            Font kpiLabelFont = workbook.createFont();
            kpiLabelFont.setBold(true);
            kpiLabelFont.setFontHeightInPoints((short) 11);
            kpiLabelStyle.setFont(kpiLabelFont);
            kpiLabelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            kpiLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            kpiLabelStyle.setBorderBottom(BorderStyle.THIN);
            kpiLabelStyle.setBorderTop(BorderStyle.THIN);
            kpiLabelStyle.setBorderLeft(BorderStyle.THIN);
            kpiLabelStyle.setBorderRight(BorderStyle.THIN);
            
            // Style KPI Valeur (Vert clair)
            CellStyle kpiValueStyle = workbook.createCellStyle();
            Font kpiValueFont = workbook.createFont();
            kpiValueFont.setBold(true);
            kpiValueFont.setFontHeightInPoints((short) 14);
            kpiValueFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            kpiValueStyle.setFont(kpiValueFont);
            kpiValueStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            kpiValueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            kpiValueStyle.setBorderBottom(BorderStyle.THIN);
            kpiValueStyle.setBorderTop(BorderStyle.THIN);
            kpiValueStyle.setBorderLeft(BorderStyle.THIN);
            kpiValueStyle.setBorderRight(BorderStyle.THIN);
            kpiValueStyle.setAlignment(HorizontalAlignment.RIGHT);
            
            // Style En-tête Tableau (Bleu, Gras, Blanc)
            CellStyle tableHeaderStyle = workbook.createCellStyle();
            Font tableHeaderFont = workbook.createFont();
            tableHeaderFont.setBold(true);
            tableHeaderFont.setFontHeightInPoints((short) 11);
            tableHeaderFont.setColor(IndexedColors.WHITE.getIndex());
            tableHeaderStyle.setFont(tableHeaderFont);
            tableHeaderStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            tableHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            tableHeaderStyle.setBorderBottom(BorderStyle.THIN);
            tableHeaderStyle.setBorderTop(BorderStyle.THIN);
            tableHeaderStyle.setBorderLeft(BorderStyle.THIN);
            tableHeaderStyle.setBorderRight(BorderStyle.THIN);
            tableHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Style Données Tableau (Bordures)
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            // Style Données Numériques
            CellStyle dataNumberStyle = workbook.createCellStyle();
            dataNumberStyle.setBorderBottom(BorderStyle.THIN);
            dataNumberStyle.setBorderTop(BorderStyle.THIN);
            dataNumberStyle.setBorderLeft(BorderStyle.THIN);
            dataNumberStyle.setBorderRight(BorderStyle.THIN);
            dataNumberStyle.setAlignment(HorizontalAlignment.RIGHT);
            
            // Style Total (Gras, Fond jaune clair)
            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);
            totalStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalStyle.setBorderBottom(BorderStyle.MEDIUM);
            totalStyle.setBorderTop(BorderStyle.MEDIUM);
            totalStyle.setBorderLeft(BorderStyle.THIN);
            totalStyle.setBorderRight(BorderStyle.THIN);

            // ========== CONTENU ==========
            
            int rowIdx = 0;

            // 1. TITRE PRINCIPAL
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeightInPoints(35);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

            // 2. SOUS-TITRE (Période)
            rowIdx++;
            Row periodRow = sheet.createRow(rowIdx++);
            Cell periodCell = periodRow.createCell(0);
            periodCell.setCellValue(messageSource.getMessage("report.period.label", 
                new Object[]{start.format(DATE_FMT), end.format(DATE_FMT)}, locale));
            periodCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

            // 3. SECTION KPIs
            rowIdx++;
            Row sectionKpiRow = sheet.createRow(rowIdx++);
            Cell sectionKpiCell = sectionKpiRow.createCell(0);
            sectionKpiCell.setCellValue(messageSource.getMessage("section.key.indicators", null, locale));
            sectionKpiCell.setCellStyle(sectionHeaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

            rowIdx++;

            // KPI 1: Production Totale
            Row kpiRow1 = sheet.createRow(rowIdx++);
            kpiRow1.setHeightInPoints(25);
            Cell kpiLabel1 = kpiRow1.createCell(0);
            kpiLabel1.setCellValue(messageSource.getMessage("kpi.production.total", null, locale));
            kpiLabel1.setCellStyle(kpiLabelStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 1));
            
            Cell kpiValue1 = kpiRow1.createCell(2);
            kpiValue1.setCellValue(totalProduction + " " + messageSource.getMessage("kpi.unit", null, locale));
            kpiValue1.setCellStyle(kpiValueStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 2, 3));

            // KPI 2: Taux de Qualité
            Row kpiRow2 = sheet.createRow(rowIdx++);
            kpiRow2.setHeightInPoints(25);
            Cell kpiLabel2 = kpiRow2.createCell(0);
            kpiLabel2.setCellValue(messageSource.getMessage("kpi.quality.rate", null, locale));
            kpiLabel2.setCellStyle(kpiLabelStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 1));
            
            Cell kpiValue2 = kpiRow2.createCell(2);
            kpiValue2.setCellValue(String.format("%.2f%%", qualityRate));
            kpiValue2.setCellStyle(kpiValueStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 2, 3));

            // KPI 3: Défauts
            Row kpiRow3 = sheet.createRow(rowIdx++);
            kpiRow3.setHeightInPoints(25);
            Cell kpiLabel3 = kpiRow3.createCell(0);
            kpiLabel3.setCellValue(messageSource.getMessage("kpi.defects.total", null, locale));
            kpiLabel3.setCellStyle(kpiLabelStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 1));
            
            Cell kpiValue3 = kpiRow3.createCell(2);
            kpiValue3.setCellValue(totalDefects + " " + messageSource.getMessage("kpi.unit", null, locale));
            kpiValue3.setCellStyle(kpiValueStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 2, 3));

            // 4. SECTION DÉTAILS PAR LIGNE
            rowIdx += 2;
            Row sectionDetailRow = sheet.createRow(rowIdx++);
            Cell sectionDetailCell = sectionDetailRow.createCell(0);
            sectionDetailCell.setCellValue(messageSource.getMessage("section.details.by.line", null, locale));
            sectionDetailCell.setCellStyle(sectionHeaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

            rowIdx++;

            // En-têtes du tableau
            Row tableHeaderRow = sheet.createRow(rowIdx++);
            tableHeaderRow.setHeightInPoints(25);
            Cell h1 = tableHeaderRow.createCell(0); 
            h1.setCellValue(messageSource.getMessage("excel.header.line", null, locale)); 
            h1.setCellStyle(tableHeaderStyle);
            
            Cell h2 = tableHeaderRow.createCell(1); 
            h2.setCellValue(messageSource.getMessage("excel.header.quantity", null, locale)); 
            h2.setCellStyle(tableHeaderStyle);
            
            Cell h3 = tableHeaderRow.createCell(2); 
            h3.setCellValue(messageSource.getMessage("excel.header.defects", null, locale)); 
            h3.setCellStyle(tableHeaderStyle);
            
            Cell h4 = tableHeaderRow.createCell(3); 
            h4.setCellValue(messageSource.getMessage("excel.header.quality", null, locale)); 
            h4.setCellStyle(tableHeaderStyle);

            // Données du tableau
            for (String line : byLine.keySet()) {
                Row dataRow = sheet.createRow(rowIdx++);
                
                Cell c1 = dataRow.createCell(0); 
                c1.setCellValue(line); 
                c1.setCellStyle(dataStyle);
                
                Cell c2 = dataRow.createCell(1); 
                c2.setCellValue(byLine.get(line)); 
                c2.setCellStyle(dataNumberStyle);
                
                int defects = defectsByLine.getOrDefault(line, 0);
                Cell c3 = dataRow.createCell(2); 
                c3.setCellValue(defects); 
                c3.setCellStyle(dataNumberStyle);
                
                // Calculer taux qualité de la ligne
                double lineQuality = byLine.get(line) > 0 
                    ? ((byLine.get(line) - defects) / byLine.get(line)) * 100 
                    : 0;
                Cell c4 = dataRow.createCell(3); 
                c4.setCellValue(String.format("%.2f%%", lineQuality)); 
                c4.setCellStyle(dataNumberStyle);
            }

            // Ligne de total
            Row totalRow = sheet.createRow(rowIdx++);
            totalRow.setHeightInPoints(25);
            Cell totalLabel = totalRow.createCell(0); 
            totalLabel.setCellValue("TOTAL GÉNÉRAL"); 
            totalLabel.setCellStyle(totalStyle);
            
            Cell totalProd = totalRow.createCell(1); 
            totalProd.setCellValue(totalProduction); 
            totalProd.setCellStyle(totalStyle);
            
            Cell totalDef = totalRow.createCell(2); 
            totalDef.setCellValue(totalDefects); 
            totalDef.setCellStyle(totalStyle);
            
            Cell totalQual = totalRow.createCell(3); 
            totalQual.setCellValue(String.format("%.2f%%", qualityRate)); 
            totalQual.setCellStyle(totalStyle);

            // Footer info
            rowIdx += 2;
            Row footerRow = sheet.createRow(rowIdx++);
            Cell footerCell = footerRow.createCell(0);
            footerCell.setCellValue("📅 Rapport généré le : " + LocalDate.now().format(DATE_FMT));
            Font footerFont = workbook.createFont();
            footerFont.setItalic(true);
            footerFont.setFontHeightInPoints((short) 9);
            footerFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            CellStyle footerStyle = workbook.createCellStyle();
            footerStyle.setFont(footerFont);
            footerCell.setCellStyle(footerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

            // Largeur des colonnes
            sheet.setColumnWidth(0, 7000);  // Ligne
            sheet.setColumnWidth(1, 5000);  // Quantité
            sheet.setColumnWidth(2, 4000);  // Défauts
            sheet.setColumnWidth(3, 4500);  // Taux qualité

            // Sauvegarder
            String excelPath = reportsDir + "/" + baseFileName + ".xlsx";
            try (FileOutputStream fos = new FileOutputStream(excelPath)) {
                workbook.write(fos);
            }
            return excelPath;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la generation du fichier Excel : " + e.getMessage(), e);
        }
    }

    public Object generateReport(Object any, Object any1, Object any2, Object any3, Object any4) {
        return null;
    }
}
