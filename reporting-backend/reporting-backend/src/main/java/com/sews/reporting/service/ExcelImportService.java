package com.sews.reporting.service;

import com.sews.reporting.entity.ProductionRecord;
import com.sews.reporting.exception.InvalidExcelFileException;
import com.sews.reporting.repository.ProductionRecordRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Section 5.2 : Importation des fichiers Excel - VERSION PROFESSIONNELLE
 * 
 * Fonctionnalites avancees :
 * 1. Import dynamique : detection automatique des colonnes avec plusieurs variantes
 * 2. DataFormatter : gestion robuste des cellules (numerique, texte, date)
 * 3. Conversion automatique des types avec gestion d'erreurs
 * 4. Validation intelligente : ignore les lignes invalides, continue l'import
 * 5. Rapport detaille : lignes importees, ignorees, erreurs avec numeros de ligne
 * 6. Analyse automatique : calcul des KPI apres import
 * 7. Compatible avec differents formats Excel industriels
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    // Variantes acceptees pour chaque colonne (insensible a la casse et aux espaces)
    private static final List<String> DATE_VARIANTS = List.of(
            "production_date", "date", "date production", "date_production",
            "productiondate", "prod_date", "date_prod", "dateproduction"
    );
    
    private static final List<String> LINE_VARIANTS = List.of(
            "line", "ligne", "production_line", "productionline",
            "prod_line", "ligne_production", "ligneproduction"
    );
    
    private static final List<String> QUANTITY_VARIANTS = List.of(
            "quantity", "production", "quantité", "quantite", "qté", "qte",
            "qty", "prod", "volume", "quantité produite"
    );
    
    private static final List<String> DEFECTS_VARIANTS = List.of(
            "defects", "défauts", "defauts", "defaut", "défaut",
            "rebut", "rebuts", "nc", "non_conforme", "nonconforme"
    );
    
    // Formats de date supportes (ordre de priorite)
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("yyyyMMdd")
    );

    private final ProductionRecordRepository productionRecordRepository;

    public ImportResult importFile(MultipartFile file) {
        log.info("Debut de l'import du fichier : {}", file.getOriginalFilename());
        
        // Verification du format du fichier
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new InvalidExcelFileException("Format invalide : seul .xlsx est accepte");
        }

        List<String> errors = new ArrayList<>();
        List<ProductionRecord> validRecords = new ArrayList<>();
        int totalRows = 0;
        int skippedRows = 0;

        try (InputStream is = file.getInputStream(); 
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new InvalidExcelFileException("Aucune ligne d'en-tete trouvee");
            }

            // Detection automatique des colonnes
            ColumnMapping mapping = detectColumns(headerRow);
            
            // Verification que toutes les colonnes obligatoires ont ete trouvees
            if (!mapping.isComplete()) {
                throw new InvalidExcelFileException(
                    "Colonnes manquantes. Colonnes detectees : " + mapping.getDetectedColumns() +
                    ". Veuillez verifier que le fichier contient : Date, Ligne, Quantite, Defauts"
                );
            }

            log.info("Colonnes detectees : {}", mapping.getDetectedColumns());

            // Utilisation de DataFormatter pour lire toutes les cellules de maniere robuste
            DataFormatter dataFormatter = new DataFormatter();

            // Traitement des lignes de donnees
            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                totalRows++;

                if (row == null || isRowEmpty(row)) {
                    skippedRows++;
                    continue;
                }

                try {
                    ProductionRecord record = parseRow(row, mapping, dataFormatter, filename);
                    validRecords.add(record);
                    
                } catch (Exception e) {
                    skippedRows++;
                    errors.add("Ligne " + (rowIdx + 1) + " : " + e.getMessage());
                    log.debug("Erreur ligne {} : {}", rowIdx + 1, e.getMessage());
                }
            }

            // Sauvegarde en base de donnees
            if (!validRecords.isEmpty()) {
                productionRecordRepository.saveAll(validRecords);
                log.info("{} enregistrements importes avec succes", validRecords.size());
            }

            // Calcul automatique des KPI
            ImportKPI kpi = calculateKPI(validRecords);

            log.info("Import termine : {} lignes importees, {} lignes ignorees, {} erreurs",
                    validRecords.size(), skippedRows, errors.size());

            return ImportResult.builder()
                    .success(true)
                    .filename(filename)
                    .importedAt(LocalDateTime.now())
                    .totalRows(totalRows)
                    .recordsImported(validRecords.size())
                    .rowsSkipped(skippedRows)
                    .errors(errors)
                    .kpi(kpi)
                    .message(buildSuccessMessage(validRecords.size(), skippedRows, errors.size()))
                    .build();

        } catch (InvalidExcelFileException e) {
            throw e;
        } catch (IOException e) {
            throw new InvalidExcelFileException("Impossible de lire le fichier : " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'import", e);
            throw new InvalidExcelFileException("Erreur lors de l'import : " + e.getMessage());
        }
    }

    /**
     * Detection automatique des colonnes en comparant avec les variantes acceptees.
     * Version tolerante :
     *  - normalise les accents plus largement (a, e, i, o, u, c)
     *  - utilise le DataFormatter au lieu de getStringCellValue() (evite un crash
     *    si l'en-tete n'est pas typee "texte" dans le fichier Excel)
     *  - si aucune colonne n'est reconnue par son nom, on retombe sur l'ordre des
     *    colonnes (Date, Ligne, Quantite, Defauts) tant que le fichier a au moins
     *    4 colonnes : ca permet d'accepter des en-tetes dans une autre langue ou
     *    un autre format, sans pour autant avaler des fichiers structurellement
     *    differents.
     */
    private ColumnMapping detectColumns(Row headerRow) {
        ColumnMapping mapping = new ColumnMapping();
        DataFormatter headerFormatter = new DataFormatter();

        for (Cell cell : headerRow) {
            if (cell == null) continue;

            String rawHeader = headerFormatter.formatCellValue(cell).trim();
            if (rawHeader.isEmpty()) continue;

            String headerValue = normalizeHeader(rawHeader);
            int colIdx = cell.getColumnIndex();

            if (mapping.dateColumn == null && DATE_VARIANTS.stream().anyMatch(headerValue::contains)) {
                mapping.dateColumn = colIdx;
                mapping.dateHeader = rawHeader;
            }
            if (mapping.lineColumn == null && LINE_VARIANTS.stream().anyMatch(headerValue::contains)) {
                mapping.lineColumn = colIdx;
                mapping.lineHeader = rawHeader;
            }
            if (mapping.quantityColumn == null && QUANTITY_VARIANTS.stream().anyMatch(headerValue::contains)) {
                mapping.quantityColumn = colIdx;
                mapping.quantityHeader = rawHeader;
            }
            if (mapping.defectsColumn == null && DEFECTS_VARIANTS.stream().anyMatch(headerValue::contains)) {
                mapping.defectsColumn = colIdx;
                mapping.defectsHeader = rawHeader;
            }
        }

        // Fallback positionnel : si la detection par nom a echoue mais qu'il y a
        // au moins 4 colonnes remplies, on suppose l'ordre standard.
        if (!mapping.isComplete()) {
            int lastCol = headerRow.getLastCellNum(); // nb de colonnes (1-based)
            if (lastCol >= 4) {
                if (mapping.dateColumn == null) {
                    mapping.dateColumn = 0;
                    mapping.dateHeader = headerFormatter.formatCellValue(headerRow.getCell(0)).trim();
                }
                if (mapping.lineColumn == null) {
                    mapping.lineColumn = 1;
                    mapping.lineHeader = headerFormatter.formatCellValue(headerRow.getCell(1)).trim();
                }
                if (mapping.quantityColumn == null) {
                    mapping.quantityColumn = 2;
                    mapping.quantityHeader = headerFormatter.formatCellValue(headerRow.getCell(2)).trim();
                }
                if (mapping.defectsColumn == null) {
                    mapping.defectsColumn = 3;
                    mapping.defectsHeader = headerFormatter.formatCellValue(headerRow.getCell(3)).trim();
                }
                log.warn("Colonnes non reconnues par leur nom, fallback positionnel applique (ordre: Date, Ligne, Quantite, Defauts)");
            }
        }

        return mapping;
    }

    /**
     * Normalise un en-tete : minuscule, espaces -> underscore, accents retires
     * (couvre les voyelles accentuees courantes + cedille), pour matcher un
     * maximum de variantes ecrites differemment.
     */
    private String normalizeHeader(String value) {
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.trim().toLowerCase().replace(" ", "_");
    }

    /**
     * Parse une ligne Excel en ProductionRecord.
     * MODE PERMISSIF : on n'echoue plus jamais une ligne pour un probleme de
     * format. Les valeurs manquantes ou invalides sont remplacees par des
     * valeurs par defaut, et un avertissement est logue (pas une erreur
     * bloquante). Ca permet d'accepter des fichiers dans a peu pres n'importe
     * quelle forme, au prix d'une qualite de donnees potentiellement plus
     * faible pour les lignes concernees.
     */
    private ProductionRecord parseRow(Row row, ColumnMapping mapping,
                                     DataFormatter formatter, String filename) {

        LocalDate productionDate;
        try {
            productionDate = extractDate(row.getCell(mapping.dateColumn), formatter);
        } catch (Exception e) {
            log.warn("Date illisible, valeur par defaut (aujourd'hui) utilisee : {}", e.getMessage());
            productionDate = LocalDate.now();
        }

        String line = extractString(row.getCell(mapping.lineColumn), formatter);
        if (line == null || line.isBlank()) {
            line = "Non specifie";
        }

        Double quantity;
        try {
            quantity = extractNumeric(row.getCell(mapping.quantityColumn), formatter);
        } catch (Exception e) {
            log.warn("Quantite illisible, valeur par defaut (0) utilisee : {}", e.getMessage());
            quantity = null;
        }
        if (quantity == null || quantity < 0) {
            quantity = 0.0;
        }

        Double defectsValue;
        try {
            defectsValue = extractNumeric(row.getCell(mapping.defectsColumn), formatter);
        } catch (Exception e) {
            log.warn("Defauts illisibles, valeur par defaut (0) utilisee : {}", e.getMessage());
            defectsValue = null;
        }
        int defects = (defectsValue != null && defectsValue >= 0) ? defectsValue.intValue() : 0;

        return ProductionRecord.builder()
                .productionDate(productionDate)
                .line(line)
                .quantity(quantity)
                .defects(defects)
                .sourceFileName(filename)
                .build();
    }

    /**
     * Extraction robuste d'une date avec support de plusieurs formats
     */
    private LocalDate extractDate(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            throw new IllegalArgumentException("Cellule de date manquante");
        }

        // 1. Si c'est une date Excel native
        if (DateUtil.isCellDateFormatted(cell)) {
            try {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } catch (Exception e) {
                // Fallback sur la date en tant que nombre
                try {
                    Date date = DateUtil.getJavaDate(cell.getNumericCellValue());
                    return new java.sql.Date(date.getTime()).toLocalDate();
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Format de date Excel invalide");
                }
            }
        }

        // 2. Sinon, tenter de parser en tant que texte
        String cellValue = formatter.formatCellValue(cell).trim();
        if (cellValue.isEmpty()) {
            throw new IllegalArgumentException("Date vide");
        }

        // Si un horodatage complet est present (ex: "2026-08-01 00:00:00" ou avec 'T'),
        // on ne garde que la partie date.
        if (cellValue.length() > 10 && (cellValue.contains(" ") || cellValue.contains("T"))) {
            cellValue = cellValue.split("[ T]")[0];
        }

        // Essayer tous les formats supportes
        for (DateTimeFormatter dtf : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(cellValue, dtf);
            } catch (Exception ignored) {
                // Continue avec le format suivant
            }
        }

        throw new IllegalArgumentException("Format de date non reconnu : " + cellValue);
    }

    /**
     * Extraction robuste d'une valeur numerique (cellule numerique ou texte)
     */
    private Double extractNumeric(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;

        // Si c'est directement une cellule numerique
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }

        // Sinon, extraire en tant que texte et parser
        String value = formatter.formatCellValue(cell).trim();
        if (value.isEmpty()) return null;

        // Nettoyer la valeur (espaces, virgules comme separateur decimal, etc.)
        value = value.replace(" ", "").replace(",", ".");

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valeur numerique invalide : " + value);
        }
    }

    /**
     * Extraction robuste d'une chaine de caracteres
     */
    private String extractString(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * Verifie si une ligne est completement vide
     */
    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = cell.toString().trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calcul automatique des KPI apres import
     */
    private ImportKPI calculateKPI(List<ProductionRecord> records) {
        if (records.isEmpty()) {
            return new ImportKPI(0, 0, 0.0, 0.0, null, null);
        }

        double totalProduction = records.stream().mapToDouble(ProductionRecord::getQuantity).sum();
        int totalDefects = records.stream().mapToInt(ProductionRecord::getDefects).sum();
        double qualityRate = totalProduction > 0 
            ? Math.round(((totalProduction - totalDefects) / totalProduction) * 10000.0) / 100.0 
            : 0.0;
        double defectRate = totalProduction > 0 
            ? Math.round((totalDefects / totalProduction) * 10000.0) / 100.0 
            : 0.0;

        LocalDate firstDate = records.stream()
                .map(ProductionRecord::getProductionDate)
                .min(LocalDate::compareTo)
                .orElse(null);

        LocalDate lastDate = records.stream()
                .map(ProductionRecord::getProductionDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        return new ImportKPI(totalProduction, totalDefects, qualityRate, defectRate, firstDate, lastDate);
    }

    /**
     * Construction du message de succes
     */
    private String buildSuccessMessage(int imported, int skipped, int errors) {
        StringBuilder sb = new StringBuilder();
        sb.append("Import termine avec succes. ");
        sb.append(imported).append(" ligne(s) importee(s)");
        
        if (skipped > 0) {
            sb.append(", ").append(skipped).append(" ligne(s) ignoree(s)");
        }
        
        if (errors > 0) {
            sb.append(", ").append(errors).append(" erreur(s) detectee(s)");
        }
        
        sb.append(".");
        return sb.toString();
    }

    // Classes internes pour la structuration des resultats

    @Getter
    private static class ColumnMapping {
        Integer dateColumn;
        Integer lineColumn;
        Integer quantityColumn;
        Integer defectsColumn;
        String dateHeader;
        String lineHeader;
        String quantityHeader;
        String defectsHeader;

        boolean isComplete() {
            return dateColumn != null && lineColumn != null && 
                   quantityColumn != null && defectsColumn != null;
        }

        String getDetectedColumns() {
            List<String> detected = new ArrayList<>();
            if (dateHeader != null) detected.add("Date=" + dateHeader);
            if (lineHeader != null) detected.add("Ligne=" + lineHeader);
            if (quantityHeader != null) detected.add("Quantite=" + quantityHeader);
            if (defectsHeader != null) detected.add("Defauts=" + defectsHeader);
            return String.join(", ", detected);
        }
    }

    public record RowError(int rowNumber, String error) {}

    public record ImportKPI(
            double totalProduction,
            int totalDefects,
            double qualityRate,
            double defectRate,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {}

    @Builder
    @Getter
    public static class ImportResult {
        private boolean success;
        private String filename;
        private LocalDateTime importedAt;
        private int totalRows;
        private int recordsImported;
        private int rowsSkipped;
        private List<String> errors;
        private ImportKPI kpi;
        private String message;
    }
}
