package com.mylog.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mylog.common.ResultCode;
import com.mylog.dto.SignUpRequest;
import com.mylog.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

    @Captor
    private ArgumentCaptor<Member> memberCaptor;

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

        // When & then
        assertThatCode(()-> memberService.saveMember(request))
            .doesNotThrowAnyException();

        // Then save 호출 검증 및 파라미터 값 검증
        verify(memberRepository, times(1)).save(memberCaptor.capture());
        Member saved = memberCaptor.getValue();

        assertThat(saved.getEmail()).isEqualTo(request.getEmail());
        assertThat(saved.getMemberName()).isEqualTo(request.getMemberName());
        assertThat(saved.getPassword()).isEqualTo(encodedPassword);
        assertThat(saved.getNickname()).isEqualTo(request.getMemberName());
    }

    @Test
    void 회원정보저장_잘못된비밀번호변경_예외던짐() {
        // Given
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test@example.com");
        request.setMemberName("TestUser");
        request.setPassword("password123");

        when(passwordEncoder.encode(request.getPassword())).thenReturn(null);

        // When
        assertThatThrownBy(()-> memberService.saveMember(request))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage(ResultCode.DATA_MISSED.getMsg());

        // Then
        verify(memberRepository, never()).save(any(Member.class));
    }
}