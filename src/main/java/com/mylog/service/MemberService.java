package com.mylog.service;


import com.mylog.dto.SignUpRequest;
import com.mylog.entity.Member;
import com.mylog.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    @Transactional
    public void saveMember(SignUpRequest request) {
        String cryptedPassword = passwordEncoder.encode(request.getPassword());
        Member member = Member.builder()
            .email(request.getEmail())
            .memberName(request.getMemberName())
            .password(cryptedPassword)
            .nickname(request.getMemberName())
            .build();

        memberRepository.save(member);
    }
}
