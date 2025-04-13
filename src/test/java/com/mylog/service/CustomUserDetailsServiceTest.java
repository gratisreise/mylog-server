package com.mylog.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private MemberRepository memberRepository;

    private Member testMember;
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "password123!";

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1L)
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .memberName("테스트유저")
            .nickname("테스트닉네임")
            .provider(OauthProvider.LOCAL)
            .build();
    }

    @Test
    void ID로_로컬_사용자_조회_성공() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("1");

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUser.class);
        assertThat(userDetails.getUsername()).isEqualTo(TEST_EMAIL);
        assertThat(userDetails.getPassword()).isEqualTo(TEST_PASSWORD);
        assertThat(((CustomUser) userDetails).getProvider()).isEqualTo(OauthProvider.LOCAL);

        verify(memberRepository).findById(1L);
    }

    @Test
    void ID로_소셜_사용자_조회_성공() {
        // given
        Member socialMember = Member.builder()
            .id(1L)
            .providerId("12345")
            .nickname(OauthProvider.KAKAO + "12345")
            .provider(OauthProvider.KAKAO)
            .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(socialMember));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("1");

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUser.class);
        assertThat(userDetails.getUsername()).isEqualTo("1");
        assertThat(((CustomUser) userDetails).getProvider()).isEqualTo(OauthProvider.SOCIAL);

        verify(memberRepository).findById(1L);
        verify(memberRepository, never()).findByEmail(any());
    }

    @Test
    void 존재하지_않는_ID로_조회시_예외발생() {
        // given
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("999"))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findById(999L);
        verify(memberRepository, never()).findByEmail(any());
    }


}