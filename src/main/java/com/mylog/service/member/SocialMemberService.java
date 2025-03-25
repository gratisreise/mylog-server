package com.mylog.service.member;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.SignUpRequest;
import com.mylog.dto.UpdateMemberRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@ServiceType(OauthProvider.SOCIAL)
@Transactional(readOnly = true)
public class SocialMemberService implements MemberService{
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void saveMember(SignUpRequest request) {
        throw new CInvalidDataException("소셜 유저는 저장 메서드 별도로 존재");
    }

    @Override
    public Member getMember(CustomUser customUser) {
        return memberRepository
            .findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);
    }

    @Override
    @Transactional
    public void updateMember(UpdateMemberRequest request, CustomUser customUser) {
        memberRepository
            .findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new)
            .update(request);
    }

    @Override
    @Transactional
    public void deleteMember(CustomUser customUser) {

    }
}
