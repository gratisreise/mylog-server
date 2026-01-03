package com.mylog.member.entity;


import com.mylog.BaseEntity;
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
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//import org.springframework.security.crypto.password.PasswordEncoder;

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

//    public void update(UpdateMemberRequest request, PasswordEncoder encoder) {
//        String password = request.password();
//        this.password = password == null ? this.password : encoder.encode(password);
//        this.memberName = request.memberName();
//        this.nickname = request.nickname();
//        this.bio = request.bio();
//    }
//
//    public void update(UpdateMemberRequest request, String profileImg) {
//        this.password = request.password();
//        this.memberName = request.memberName();
//        this.nickname = request.nickname();
//        this.bio = request.bio();
//        this.profileImg = profileImg;
//    }
//
//    public void update(OAuth2UserInfo userInfo, OauthProvider oauthProvider) {
//        this.provider = oauthProvider;
//        this.providerId = userInfo.getId();
//        this.memberName = userInfo.getName();
//        this.password = userInfo.getId() + UUID.randomUUID();
//        this.nickname = userInfo.getId() + oauthProvider;
//        this.profileImg = userInfo.getImageUrl();
//    }

    public boolean isOwnedBy(Long memberId) {
        return Objects.equals(id, memberId);
    }

    public void update(Member member) {
        this.password = member.getPassword();
        this.memberName = member.getMemberName();
        this.nickname = member.getNickname();
        this.bio = member.getBio();
        this.profileImg = member.getProfileImg();
    }
}
