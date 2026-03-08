package com.mylog.domain.member.service;

import com.mylog.domain.member.Member;
import com.mylog.domain.member.dto.UpdateMemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class MemberWriter {

    public void update(Member member, UpdateMemberRequest request, String imageUrl) {
        member.updateProfile(request.nickname(), request.bio(), imageUrl);
    }

    public void delete(Member member) {
        member.delete();
    }
}
