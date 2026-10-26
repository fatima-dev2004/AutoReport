package com.sews.reporting.repository;

import com.sews.reporting.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
    
    Optional<SystemSettings> findByKey(String key);
    
    List<SystemSettings> findByCategory(String category);
    
    boolean existsByKey(String key);
}
