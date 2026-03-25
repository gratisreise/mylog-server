package com.mylog.domain.member.dto;

import com.mylog.common.enums.OauthProvider;
import com.mylog.domain.member.entity.Member;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
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
    LocalDateTime updatedAt) {
  public static MemberResponse from(Member member) {
    return MemberResponse.builder()
        .id(member.getId())
        .email(member.getEmail())
        .memberName(member.getMemberName())
        .nickname(member.getNickname())
        .profileImg(member.getProfileImg())
        .bio(member.getBio())
        .provider(member.getProvider())
        .providerId(member.getProviderId())
        .createdAt(member.getCreatedAt())
        .updatedAt(member.getUpdatedAt())
        .build();
  }
}
