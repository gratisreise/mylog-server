package com.mylog.service;


import com.mylog.dto.SignUpRequest;
import com.mylog.entity.Member;
import com.mylog.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    //회원가입
    @Transactional
    public void saveMember(SignUpRequest request) {
        Member member = Member.builder()
            .email(request.getEmail())
            .memberName(request.getMemberName())
            .password(request.getPassword())
            .nickname(request.getMemberName())
            .build();

        memberRepository.save(member);
    }
}
