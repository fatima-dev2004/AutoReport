package com.sews.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Destinataire email additionnel configurable depuis la page Parametres
 * (section 5.8 : destinataires des rapports planifies)
 */
@Entity
@Table(name = "email_recipients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;
}
