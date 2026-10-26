// EXEMPLE : Comment créer un Excel avec Design Professionnel

private String generateEnhancedExcelReport(String title, LocalDate start, LocalDate end,
                                          int totalProduction, int totalDefects, double qualityRate,
                                          Map<String, Double> byLine, Map<String, Integer> defectsByLine,
                                          String baseFileName) {
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Rapport");

        // ========== STYLES ==========
        
        // Style En-tête Principal (Bleu, Gras, Blanc)
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.WHITE.getIndex());
        titleStyle.setFont(titleFont);
        titleStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        // Style En-tête Tableau (Gris, Gras)
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Style KPI (Fond vert clair pour valeurs positives)
        CellStyle kpiStyle = workbook.createCellStyle();
        kpiStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        kpiStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font kpiFont = workbook.createFont();
        kpiFont.setBold(true);
        kpiStyle.setFont(kpiFont);

        // Style Données (Bordures)
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // ========== CONTENU ==========
        
        int rowIdx = 0;

        // 1. TITRE
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 2)); // Merge 3 colonnes

        // 2. PÉRIODE
        rowIdx++;
        Row periodRow = sheet.createRow(rowIdx++);
        Cell periodCell = periodRow.createCell(0);
        periodCell.setCellValue("Période : " + start + " - " + end);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 2));

        // 3. KPIs
        rowIdx++;
        Row kpiLabelRow1 = sheet.createRow(rowIdx++);
        kpiLabelRow1.createCell(0).setCellValue("Production Totale");
        Cell kpiValue1 = kpiLabelRow1.createCell(1);
        kpiValue1.setCellValue(totalProduction);
        kpiValue1.setCellStyle(kpiStyle);

        Row kpiLabelRow2 = sheet.createRow(rowIdx++);
        kpiLabelRow2.createCell(0).setCellValue("Nombre de Défauts");
        Cell kpiValue2 = kpiLabelRow2.createCell(1);
        kpiValue2.setCellValue(totalDefects);
        kpiValue2.setCellStyle(kpiStyle);

        Row kpiLabelRow3 = sheet.createRow(rowIdx++);
        kpiLabelRow3.createCell(0).setCellValue("Taux de Qualité");
        Cell kpiValue3 = kpiLabelRow3.createCell(1);
        kpiValue3.setCellValue(String.format("%.2f%%", qualityRate));
        kpiValue3.setCellStyle(kpiStyle);

        // 4. TABLEAU DÉTAILLÉ
        rowIdx++;
        Row tableHeaderRow = sheet.createRow(rowIdx++);
        Cell h1 = tableHeaderRow.createCell(0); h1.setCellValue("Ligne"); h1.setCellStyle(headerStyle);
        Cell h2 = tableHeaderRow.createCell(1); h2.setCellValue("Quantité"); h2.setCellStyle(headerStyle);
        Cell h3 = tableHeaderRow.createCell(2); h3.setCellValue("Défauts"); h3.setCellStyle(headerStyle);

        // Données
        for (String line : byLine.keySet()) {
            Row row = sheet.createRow(rowIdx++);
            Cell c1 = row.createCell(0); c1.setCellValue(line); c1.setCellStyle(dataStyle);
            Cell c2 = row.createCell(1); c2.setCellValue(byLine.get(line)); c2.setCellStyle(dataStyle);
            Cell c3 = row.createCell(2); c3.setCellValue(defectsByLine.getOrDefault(line, 0)); c3.setCellStyle(dataStyle);
        }

        // Largeur colonnes
        sheet.setColumnWidth(0, 5000);  // 5000 unités = ~20 caractères
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);

        // Sauvegarder
        String excelPath = reportsDir + "/" + baseFileName + ".xlsx";
        try (FileOutputStream fos = new FileOutputStream(excelPath)) {
            workbook.write(fos);
        }
        return excelPath;

    } catch (Exception e) {
        log.error("Erreur génération Excel", e);
        throw new RuntimeException("Erreur génération Excel", e);
    }
}
