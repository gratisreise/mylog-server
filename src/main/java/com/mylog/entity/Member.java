package com.mylog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@EnableJpaAuditing
@NoArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String email;

    @Column(length = 20, nullable = false)
    private String password;

    @Column(length = 30, nullable = false)
    private String memberName;

    @Column(length = 15)
    private String nickname;

    @Column(length = 300)
    private String profileImg;

    @Column(length = 200)
    private String bio;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
