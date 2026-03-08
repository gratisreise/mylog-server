package com.mylog.domain.member.entity;

import com.mylog.common.db.BaseEntity;
import com.mylog.common.enums.OauthProvider;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(indexes = {
    @Index(name = "idx_provider_providerId", columnList = "provider,providerId", unique = true)
})
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String email;

    @Column(length = 150)
    private String password;

    @Column(length = 30, nullable = false)
    private String memberName;

    @Column(length = 100, unique = true)
    private String nickname;

    @Column(length = 300, nullable = false)
    private String profileImg;

    @Column(length = 200)
    private String bio;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private OauthProvider provider;

    @Column(length = 200)
    private String providerId;

    private LocalDateTime deletedAt;

    public void validatePassword(String rawPassword, PasswordEncoder encoder) {
        if (!encoder.matches(rawPassword, this.password)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    public void updateProfile(String nickname, String bio, String profileImg) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (bio != null) {
            this.bio = bio;
        }
        if (profileImg != null) {
            this.profileImg = profileImg;
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
