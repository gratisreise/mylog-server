package com.mylog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@NoArgsConstructor
@EnableJpaAuditing
public class Social {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name ="member_id")
    private Member member;

    @Column(length = 30)
    private String provider;

    @Column(length = 200)
    private String providerId;

    @CreatedDate
    private LocalDateTime createdAt;
}
