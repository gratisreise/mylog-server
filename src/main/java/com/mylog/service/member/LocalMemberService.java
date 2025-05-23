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

        if(memberRepository.existsByEmail(request.getEmail())){
            throw new CMissingDataException("가입된 사용자입니다.");
        }

        String cryptedPassword = passwordEncoder.encode(request.getPassword());
        if (cryptedPassword == null) {
            throw new CMissingDataException("비밀번호 암호화를 실패했습니다.");
        }

        Member member = generateMember(request, cryptedPassword);
        memberRepository.save(member);
    }


    @Override
    public Member getMember(CustomUser customUser) {
        String email = customUser.getUsername();
        return memberRepository.findByEmail(email)
            .orElseThrow(CMissingDataException::new);
    }

    @Override
    @Transactional
    public void updateMember(UpdateMemberRequest request, CustomUser customUser) {
        String email = customUser.getUsername();
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(CMissingDataException::new);
        member.update(request);
    }

    @Override
    @Transactional
    public void deleteMember(CustomUser customUser) {
        memberRepository.deleteByEmail(customUser.getUsername());
    }

    private Member generateMember(SignUpRequest request, String cryptedPassword) {
        return Member.builder()
            .email(request.getEmail())
            .memberName(request.getMemberName())
            .password(cryptedPassword)
            .nickname(request.getEmail())
            .provider(OauthProvider.LOCAL)
            .providerId(request.getEmail())
            .build();
    }
}

