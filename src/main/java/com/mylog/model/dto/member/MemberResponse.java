package com.mylog.model.dto.member;


import com.mylog.enums.OauthProvider;
import com.mylog.model.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

public record MemberResponse(
    Long id,
    String email,
    String memberName,
    String nickname,
    String profileImg,
    String bio,
    OauthProvider provider,
    String providerId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public MemberResponse(Member member) {
        this(
            member.getId(), member.getEmail(), member.getMemberName(),
            member.getNickname(), member.getProfileImg(), member.getBio(),
            member.getProvider(), member.getProviderId(), member.getCreatedAt(),
            member.getUpdatedAt()
        );
    }
}


