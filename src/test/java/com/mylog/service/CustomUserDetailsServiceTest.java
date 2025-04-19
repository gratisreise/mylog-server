package com.mylog.service;

import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private MemberRepository memberRepository;

    private String username = "testUser";
    private Member member;

    @BeforeEach
    void setUp() {
        // 테스트용 Member 객체 초기화
        member = new Member();
        member.setNickname(username);
        member.setPassword("encodedPassword");
    }

    @Test
    void 사용자이름으로_조회_성공_유저디테일반환() {
        // Given
        when(memberRepository.findByNickname(username)).thenReturn(Optional.of(member));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertThat(userDetails).isInstanceOf(CustomUser.class);
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
            .isEqualTo("ROLE_USER");

    }

    @Test
    void 사용자이름으로_조회_실패_예외발생() {
        // Given
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(username))
            .isInstanceOf(CMissingDataException.class);
    }

}