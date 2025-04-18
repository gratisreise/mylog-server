package com.mylog.service.member;

import com.mylog.dto.SignUpRequest;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import com.mylog.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommonMemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    @Value("aws.s3.basic")
    private String basicImageUrl;

    public void saveMember(SignUpRequest request){
        if(memberRepository.existsByEmail(request.getEmail())){
            throw new CMissingDataException("이미 존재하는 이메일입니다.");
        }

        String cryptedPassword = passwordEncoder.encode(request.getPassword());
        log.info("cryptedPassword: {}", cryptedPassword);

        if (cryptedPassword == null) {
            throw new CMissingDataException("비밀번호 암호화를 실패했습니다.");
        }

        Member member = Member.builder()
            .email(request.getEmail())
            .memberName(request.getMemberName())
            .password(cryptedPassword)
            .nickname(request.getEmail())
            .provider(OauthProvider.LOCAL)
            .providerId(request.getEmail()+OauthProvider.LOCAL)
            .profileImg(basicImageUrl)
            .build();

        log.info("member: {}", member.toString());
        memberRepository.save(member);
    }



}
