package com.mylog.service.member;

import com.mylog.dto.SignUpRequest;
import com.mylog.repository.MemberRepository;
import com.mylog.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonMemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    public void saveMember(SignUpRequest request){

    }
}
