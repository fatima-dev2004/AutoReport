package com.sews.reporting.service;

import com.sews.reporting.dto.SystemSettingsDto;
import com.sews.reporting.entity.SystemSettings;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.SystemSettingsRepository;
import com.sews.reporting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemSettingsService {

    private final SystemSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    public SystemSettingsDto getAllSettings() {
        List<SystemSettings> allSettings = settingsRepository.findAll();
        
        Map<String, String> smtp = new HashMap<>();
        Map<String, String> company = new HashMap<>();
        Map<String, String> scheduling = new HashMap<>();
        Map<String, String> general = new HashMap<>();
        
        for (SystemSettings setting : allSettings) {
            String category = setting.getCategory() != null ? setting.getCategory() : "general";
            switch (category) {
                case "smtp":
                    smtp.put(setting.getKey(), setting.getValue());
                    break;
                case "company":
                    company.put(setting.getKey(), setting.getValue());
                    break;
                case "scheduling":
                    scheduling.put(setting.getKey(), setting.getValue());
                    break;
                default:
                    general.put(setting.getKey(), setting.getValue());
                    break;
            }
        }
        
        return SystemSettingsDto.builder()
                .smtp(smtp)
                .company(company)
                .scheduling(scheduling)
                .general(general)
                .build();
    }

    public String getSetting(String key) {
        return settingsRepository.findByKey(key)
                .map(SystemSettings::getValue)
                .orElse(null);
    }

    public String getSetting(String key, String defaultValue) {
        return settingsRepository.findByKey(key)
                .map(SystemSettings::getValue)
                .orElse(defaultValue);
    }

    @Transactional
    public void updateSettings(SystemSettingsDto settingsDto, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        
        // Update SMTP settings
        if (settingsDto.getSmtp() != null) {
            updateCategorySettings(settingsDto.getSmtp(), "smtp", user, now);
        }
        
        // Update Company settings
        if (settingsDto.getCompany() != null) {
            updateCategorySettings(settingsDto.getCompany(), "company", user, now);
        }
        
        // Update Scheduling settings
        if (settingsDto.getScheduling() != null) {
            updateCategorySettings(settingsDto.getScheduling(), "scheduling", user, now);
        }
        
        // Update General settings
        if (settingsDto.getGeneral() != null) {
            updateCategorySettings(settingsDto.getGeneral(), "general", user, now);
        }
    }

    @Transactional
    public void setSetting(String key, String value, String category, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        
        SystemSettings setting = settingsRepository.findByKey(key)
                .orElse(SystemSettings.builder()
                        .key(key)
                        .category(category)
                        .build());
        
        setting.setValue(value);
        setting.setUpdatedAt(LocalDateTime.now());
        setting.setUpdatedBy(user);
        
        settingsRepository.save(setting);
    }

    private void updateCategorySettings(Map<String, String> settings, String category, User user, LocalDateTime now) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            SystemSettings setting = settingsRepository.findByKey(key)
                    .orElse(SystemSettings.builder()
                            .key(key)
                            .category(category)
                            .build());
            
            setting.setValue(value);
            setting.setUpdatedAt(now);
            setting.setUpdatedBy(user);
            
            settingsRepository.save(setting);
        }
    }

    @Transactional
    public void initializeDefaultSettings() {
        // SMTP defaults
        createSettingIfNotExists("smtp.host", "smtp.gmail.com", "smtp", "SMTP Server Host");
        createSettingIfNotExists("smtp.port", "587", "smtp", "SMTP Server Port");
        createSettingIfNotExists("smtp.username", "", "smtp", "SMTP Username");
        createSettingIfNotExists("smtp.password", "", "smtp", "SMTP Password");
        createSettingIfNotExists("smtp.from", "noreply@sews.com", "smtp", "Email From Address");
        createSettingIfNotExists("smtp.fromName", "SEWS Reporting", "smtp", "Email From Name");
        
        // Company defaults
        createSettingIfNotExists("company.name", "SEWS Cabind", "company", "Company Name");
        createSettingIfNotExists("company.logo", "", "company", "Company Logo URL");
        createSettingIfNotExists("company.address", "", "company", "Company Address");
        createSettingIfNotExists("company.phone", "", "company", "Company Phone");
        createSettingIfNotExists("company.email", "contact@sews.com", "company", "Company Email");
        
        // Scheduling defaults
        createSettingIfNotExists("scheduling.defaultDays", "MONDAY,FRIDAY", "scheduling", "Default Report Days");
        createSettingIfNotExists("scheduling.defaultTime", "08:00", "scheduling", "Default Report Time");
        createSettingIfNotExists("scheduling.timezone", "Africa/Casablanca", "scheduling", "System Timezone");
        
        // General defaults
        createSettingIfNotExists("general.dateFormat", "dd/MM/yyyy", "general", "Date Format");
        createSettingIfNotExists("general.language", "fr", "general", "System Language");
        createSettingIfNotExists("general.reportsRetentionDays", "90", "general", "Reports Retention Days");
    }

    private void createSettingIfNotExists(String key, String value, String category, String description) {
        if (!settingsRepository.existsByKey(key)) {
            SystemSettings setting = SystemSettings.builder()
                    .key(key)
                    .value(value)
                    .category(category)
                    .description(description)
                    .updatedAt(LocalDateTime.now())
                    .build();
            settingsRepository.save(setting);
        }
    }
}
