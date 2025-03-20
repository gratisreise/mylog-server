package com.mylog.service;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.dto.SignUpRequest;
import com.mylog.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;


class MemberServiceTest {


    @InjectMocks
    private MemberService memberService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 회원정보저장_유요한회원정보_성공() {
        // Given
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test@example.com");
        request.setMemberName("TestUser");
        request.setPassword("password123");

        String encodedPassword = "encodedPassword123";

        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

        // When
        memberService.saveMember(request);

        // Then
        verify(memberRepository, times(1)).save(argThat(member ->
            member.getEmail().equals(request.getEmail()) &&
                member.getMemberName().equals(request.getMemberName()) &&
                member.getPassword().equals(encodedPassword) &&
                member.getNickname().equals(request.getMemberName())
        ));
    }

    @Test
    void 회원정보저장_잘못된비밀번호변경_실패() {
        // Given
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test@example.com");
        request.setMemberName("TestUser");
        request.setPassword("password123");

        when(passwordEncoder.encode(request.getPassword())).thenReturn(null);

        // When
        memberService.saveMember(request);

        // Then
        verify(memberRepository, times(0)).save(argThat(member -> true));
    }
}