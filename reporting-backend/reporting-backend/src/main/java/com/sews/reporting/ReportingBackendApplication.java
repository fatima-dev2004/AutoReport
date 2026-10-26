package com.sews.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReportingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportingBackendApplication.class, args);
    }
}
