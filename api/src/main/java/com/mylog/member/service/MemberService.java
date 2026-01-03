package com.mylog.member.service;


import com.mylog.auth.CustomUser;
import com.mylog.enums.ErrorMessage;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.member.dto.MemberResponse;
import com.mylog.member.dto.MemberUpdateRequest;
import com.mylog.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberReader memberReader;
    private final MemberWriter memberWriter;
    private final PasswordEncoder encoder;

    public MemberResponse getMember(CustomUser customUser) {
        Member member = memberReader.getById(customUser.getMemberId());
        return MemberResponse.from(member);
    }

    @Transactional
    public void updateMember(MemberUpdateRequest request, String imageUrl, CustomUser customUser) {
        Member member = request.toEntity(encoder, imageUrl);
        Long memberId = customUser.getMemberId();
        memberWriter.updateMember(member, memberId);
    }

    @Transactional
    public void deleteMember(CustomUser customUser) {
        Long memberId = customUser.getMemberId();
        memberWriter.deleteMember(memberId);
        memberReader.isExists(memberId);
    }


}
