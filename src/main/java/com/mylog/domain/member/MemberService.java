package com.mylog.domain.member;

import com.mylog.domain.member.dto.MemberResponse;
import com.mylog.domain.member.dto.UpdateMemberRequest;
import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.member.service.MemberWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberReader memberReader;
    private final MemberWriter memberWriter;

    public MemberResponse getMember(Long memberId) {
        Member member = memberReader.getById(memberId);
        return new MemberResponse(member);
    }

    public void updateMember(UpdateMemberRequest request, String imageUrl, Long memberId) {
        Member member = memberReader.getById(memberId);
        memberWriter.update(member, request, imageUrl);
    }

    public void deleteMember(Long memberId) {
        Member member = memberReader.getById(memberId);
        memberWriter.delete(member);
    }
}
