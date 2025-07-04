package com.mylog.model.entity;

import com.mylog.model.dto.member.SignUpRequest;
import com.mylog.model.dto.member.UpdateMemberRequest;
import com.mylog.enums.OauthProvider;
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
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(indexes = {
    @Index(name = "idx_provider_providerId", columnList = "provider,providerId", unique = true)
})
@ToString
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Member(SignUpRequest request, String cryptedPassword, String basicImageUrl) {
        this.email = request.getEmail();
        this.memberName = request.getMemberName();
        this.nickname = request.getEmail();
        this.providerId = request.getEmail() + OauthProvider.LOCAL;
        this.provider = OauthProvider.LOCAL;
        this.profileImg = basicImageUrl;
        this.password = cryptedPassword;
    }

    public void update(UpdateMemberRequest request) {
        this.password = request.getPassword();
        this.memberName = request.getMemberName();
        this.nickname = request.getNickname();
        this.bio = request.getBio();
    }

    public void update(UpdateMemberRequest request, String profileImg) {
        this.password = request.getPassword();
        this.memberName = request.getMemberName();
        this.nickname = request.getNickname();
        this.bio = request.getBio();
        this.profileImg = profileImg;
    }
}
