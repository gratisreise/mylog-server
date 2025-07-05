package com.mylog.model.dto.member;


import com.mylog.enums.OauthProvider;
import com.mylog.model.entity.Member;
import java.time.LocalDateTime;

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


