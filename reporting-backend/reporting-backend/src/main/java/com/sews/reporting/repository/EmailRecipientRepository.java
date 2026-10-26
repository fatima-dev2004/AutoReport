package com.sews.reporting.repository;

import com.sews.reporting.entity.EmailRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRecipientRepository extends JpaRepository<EmailRecipient, Long> {
}
