package com.mylog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@EnableJpaAuditing
@NoArgsConstructor
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, unique = true)
    private String tagName;

    @CreatedDate
    private LocalDate createdAt;
}
