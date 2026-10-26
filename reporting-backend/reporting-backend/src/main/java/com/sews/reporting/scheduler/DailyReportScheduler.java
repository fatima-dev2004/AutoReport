package com.sews.reporting.scheduler;

import com.sews.reporting.entity.ProductionReport;
import com.sews.reporting.entity.ReportSchedule;
import com.sews.reporting.repository.ReportScheduleRepository;
import com.sews.reporting.service.EmailService;
import com.sews.reporting.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Section 5.6 (Envoi automatique) + 5.8 (Planification des rapports)
 *
 * Fonctionnement par defaut : tous les jours a 08:00 (configurable via
 * app.scheduler.daily-report-cron dans application.properties) :
 *   1. generation du rapport
 *   2. creation du fichier PDF
 *   3. envoi aux responsables
 *
 * En complement, les planifications personnalisees (ReportSchedule) creees par
 * l'administrateur sont verifiees a chaque execution.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReportScheduler {

    private final ReportGenerationService reportGenerationService;
    private final EmailService emailService;
    private final ReportScheduleRepository reportScheduleRepository;

    @Scheduled(cron = "${app.scheduler.daily-report-cron}")
    public void generateAndSendDailyReport() {
        log.info("Debut de la generation automatique du rapport quotidien de production");

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        try {
            ProductionReport report = reportGenerationService.generateProductionReport(yesterday, today, null);

            for (ReportSchedule schedule : reportScheduleRepository.findByActiveTrue()) {
                if (schedule.getReportType() == ProductionReport.ReportType.PRODUCTION
                        && schedule.getRecipients() != null && !schedule.getRecipients().isBlank()) {

                    String[] recipients = schedule.getRecipients().split(",");
                    emailService.sendReportByEmail(
                            recipients,
                            report.getTitle(),
                            "Veuillez trouver ci-joint le rapport de production du " + today + ".",
                            report.getPdfFilePath()
                    );
                }
            }

            log.info("Rapport quotidien genere et envoye avec succes : {}", report.getTitle());
        } catch (Exception e) {
            log.error("Erreur lors de la generation/envoi du rapport quotidien", e);
        }
    }
}
