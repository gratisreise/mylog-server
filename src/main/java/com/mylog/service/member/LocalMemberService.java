package com.mylog.service.member;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.SignUpRequest;
import com.mylog.dto.UpdateMemberRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@ServiceType(OauthProvider.LOCAL)
@Transactional(readOnly = true)
public class LocalMemberService implements MemberService{
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    @Transactional
    public void saveMember(SignUpRequest request) {
        String cryptedPassword = passwordEncoder.encode(request.getPassword());
        log.info("cryptedPassword: {}", cryptedPassword);
        if (cryptedPassword == null) {
            throw new CMissingDataException("비밀번호 암호화를 실패했습니다.");
        }
        Member member = Member.builder()
            .email(request.getEmail())
            .memberName(request.getMemberName())
            .password(cryptedPassword)
            .nickname(request.getMemberName())
            .provider(OauthProvider.LOCAL)
            .providerId(request.getEmail())
            .build();
        log.info("member: {}", member.toString());
        memberRepository.save(member);
    }

    @Override
    //사용자 정보 조회
    public Member getMember(CustomUser customUser) {
        return null;
    }

    @Override
    @Transactional
    public void updateMember(UpdateMemberRequest request, CustomUser customUser) {

    }

    @Override
    @Transactional
    public void deleteMember(CustomUser customUser) {

    }
}

