package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Member;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Comprehensive unit tests for CustomUserDetailsService
 * Tests UserDetails loading, Spring Security integration, and authorization
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private MemberReadService memberReadService;

    private Member testMember;
    private Member oauthMember;
    private static final String TEST_NICKNAME = "testuser";
    private static final String OAUTH_NICKNAME = "oauthuser";
    private static final String DEFAULT_ENCRYPTED_PASSWORD = "$2a$10$encrypted.password.hash";
    private static final String OAUTH_ENCRYPTED_PASSWORD = "$2a$10$encrypted.password.hash";

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .nickname(TEST_NICKNAME)
                .email("test@example.com")
                .password(DEFAULT_ENCRYPTED_PASSWORD)
                .memberName("Test User")
                .bio("Test bio")
                .profileImg("https://example.com/profile.jpg")
                .provider(OauthProvider.LOCAL)
                .providerId("test@example.com" + OauthProvider.LOCAL)
                .build();

        oauthMember = Member.builder()
                .id(2L)
                .nickname(OAUTH_NICKNAME)
                .email("oauth@example.com")
                .password(OAUTH_ENCRYPTED_PASSWORD) // OAuth users don't have password
                .memberName("OAuth User")
                .bio("OAuth bio")
                .profileImg("https://example.com/oauth-profile.jpg")
                .provider(OauthProvider.GOOGLE)
                .providerId("oauth@example.com" + OauthProvider.GOOGLE)
                .build();
    }

    @Test
    @DisplayName("이메일로 UserDetails 로딩 성공 - 로컬 사용자")
    void loadUserByUsername_이메일_로컬_사용자_성공() {
        // Given
        String email = "test@example.com";
        when(memberReadService.getByEmail(email)).thenReturn(testMember);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUser.class);
        
        CustomUser customUser = (CustomUser) userDetails;
        assertThat(customUser.getUsername()).isEqualTo("1");
        assertThat(customUser.getPassword()).isEqualTo(DEFAULT_ENCRYPTED_PASSWORD);
        assertThat(customUser.getMemberId()).isEqualTo(1L);
        assertThat(customUser.getProvider()).isEqualTo(OauthProvider.LOCAL);
        
        // Verify authorities
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        
        verify(memberReadService).getByEmail(email);
    }

    @Test
    @DisplayName("ID로 UserDetails 로딩 성공 - 토큰 검증")
    void loadUserByUsername_ID_토큰_검증_성공() {
        // Given
        String memberId = "2";
        when(memberReadService.getById(2L)).thenReturn(oauthMember);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(memberId);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUser.class);
        
        CustomUser customUser = (CustomUser) userDetails;
        assertThat(customUser.getUsername()).isEqualTo("2");
        assertThat(customUser.getMemberId()).isEqualTo(2L);
        assertThat(customUser.getProvider()).isEqualTo(OauthProvider.GOOGLE);
        
        // Verify authorities
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        
        verify(memberReadService).getById(2L);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 UserDetails 로딩 실패")
    void loadUserByUsername_존재하지_않는_이메일_실패() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(memberReadService.getByEmail(nonExistentEmail))
                .thenThrow(new CMissingDataException("사용자를 찾을 수 없습니다."));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(nonExistentEmail))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
        
        verify(memberReadService).getByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 UserDetails 로딩 실패")
    void loadUserByUsername_존재하지_않는_ID_실패() {
        // Given
        String nonExistentId = "999";
        when(memberReadService.getById(999L))
                .thenThrow(new CMissingDataException("사용자를 찾을 수 없습니다."));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(nonExistentId))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
        
        verify(memberReadService).getById(999L);
    }

    @Test
    @DisplayName("잘못된 ID 형식으로 UserDetails 로딩 실패")
    void loadUserByUsername_잘못된_ID_형식_실패() {
        // Given
        String invalidIdFormat = "abc";

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(invalidIdFormat))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("UserDetails 계정 상태 검증")
    void loadUserByUsername_계정_상태_검증() {
        // Given
        String email = "test@example.com";
        when(memberReadService.getByEmail(email)).thenReturn(testMember);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("권한 설정 검증 - ROLE_USER 기본 권한")
    void loadUserByUsername_권한_설정_검증() {
        // Given
        String email = "test@example.com";
        when(memberReadService.getByEmail(email)).thenReturn(testMember);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSize(1);
        
        GrantedAuthority authority = authorities.iterator().next();
        assertThat(authority.getAuthority()).isEqualTo("ROLE_USER");
        assertThat(authority).isInstanceOf(SimpleGrantedAuthority.class);
    }

    @Test
    @DisplayName("CustomUser 객체 생성 검증")
    void loadUserByUsername_CustomUser_객체_생성_검증() {
        // Given
        String email = "test@example.com";
        when(memberReadService.getByEmail(email)).thenReturn(testMember);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails).isInstanceOf(CustomUser.class);
        
        CustomUser customUser = (CustomUser) userDetails;
        
        // CustomUser 특화 필드 검증
        assertThat(customUser.getMemberId()).isEqualTo(testMember.getId());
        assertThat(customUser.getProvider()).isEqualTo(testMember.getProvider());
        
        // UserDetails 인터페이스 구현 검증
        assertThat(customUser.getUsername()).isEqualTo(testMember.getId().toString());
        assertThat(customUser.getPassword()).isEqualTo(testMember.getPassword());
    }

    @Test
    @DisplayName("다양한 OAuth 제공자 사용자 로딩")
    void loadUserByUsername_다양한_OAuth_제공자() {
        // Given - KAKAO 사용자
        Member kakaoMember = Member.builder()
                .id(3L)
                .nickname("kakaouser")
                .email("kakao@example.com")
                .password("kakohash")
                .memberName("Kakao User")
                .bio("Kakao bio")
                .profileImg("https://example.com/kakao-profile.jpg")
                .provider(OauthProvider.KAKAO)
                .providerId("kakao@example.com" + OauthProvider.KAKAO)
                .build();

        // Given - NAVER 사용자 ID로 조회
        Member naverMember = Member.builder()
                .id(4L)
                .nickname("naveruser")
                .email("naver@example.com")
                .password("naver.hash")
                .memberName("Naver User")
                .bio("Naver bio")
                .profileImg("https://example.com/naver-profile.jpg")
                .provider(OauthProvider.NAVER)
                .providerId("naver@example.com" + OauthProvider.NAVER)
                .build();

        when(memberReadService.getByEmail("kakao@example.com")).thenReturn(kakaoMember);
        when(memberReadService.getById(4L)).thenReturn(naverMember);

        // When
        UserDetails kakaoUserDetails = customUserDetailsService.loadUserByUsername("kakao@example.com");
        UserDetails naverUserDetails = customUserDetailsService.loadUserByUsername("4");

        // Then
        CustomUser kakaoCustomUser = (CustomUser) kakaoUserDetails;
        CustomUser naverCustomUser = (CustomUser) naverUserDetails;
        
        assertThat(kakaoCustomUser.getProvider()).isEqualTo(OauthProvider.KAKAO);
        assertThat(naverCustomUser.getProvider()).isEqualTo(OauthProvider.NAVER);
        
        // 모든 OAuth 사용자는 동일한 권한을 가져야 함
        assertThat(kakaoUserDetails.getAuthorities()).hasSize(1);
        assertThat(naverUserDetails.getAuthorities()).hasSize(1);
        
        verify(memberReadService).getByEmail("kakao@example.com");
        verify(memberReadService).getById(4L);
    }

    @Test
    @DisplayName("멤버 서비스 호출 실패 시 예외 전파")
    void loadUserByUsername_멤버_서비스_예외_전파() {
        // Given
        String email = "test@example.com";
        when(memberReadService.getByEmail(email))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");
        
        verify(memberReadService).getByEmail(email);
    }

    @Test
    @DisplayName("연속된 사용자 조회 요청 처리 - 이메일")
    void loadUserByUsername_연속_요청_처리_이메일() {
        // Given
        String email = "test@example.com";
        when(memberReadService.getByEmail(email)).thenReturn(testMember);

        // When
        UserDetails firstCall = customUserDetailsService.loadUserByUsername(email);
        UserDetails secondCall = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(firstCall).isNotNull();
        assertThat(secondCall).isNotNull();
        
        // 각 호출은 새로운 CustomUser 인스턴스를 생성해야 함
        assertThat(firstCall).isNotSameAs(secondCall);
        
        // 하지만 내용은 동일해야 함
        CustomUser firstCustomUser = (CustomUser) firstCall;
        CustomUser secondCustomUser = (CustomUser) secondCall;
        
        assertThat(firstCustomUser.getMemberId()).isEqualTo(secondCustomUser.getMemberId());
        assertThat(firstCustomUser.getUsername()).isEqualTo(secondCustomUser.getUsername());
        assertThat(firstCustomUser.getProvider()).isEqualTo(secondCustomUser.getProvider());
        
        // 멤버 서비스는 두 번 호출되어야 함
        verify(memberReadService, times(2)).getByEmail(email);
    }

    @Test
    @DisplayName("연속된 사용자 조회 요청 처리 - ID")
    void loadUserByUsername_연속_요청_처리_ID() {
        // Given
        String memberId = "1";
        when(memberReadService.getById(1L)).thenReturn(testMember);

        // When
        UserDetails firstCall = customUserDetailsService.loadUserByUsername(memberId);
        UserDetails secondCall = customUserDetailsService.loadUserByUsername(memberId);

        // Then
        assertThat(firstCall).isNotNull();
        assertThat(secondCall).isNotNull();
        
        // 각 호출은 새로운 CustomUser 인스턴스를 생성해야 함
        assertThat(firstCall).isNotSameAs(secondCall);
        
        // 하지만 내용은 동일해야 함
        CustomUser firstCustomUser = (CustomUser) firstCall;
        CustomUser secondCustomUser = (CustomUser) secondCall;
        
        assertThat(firstCustomUser.getMemberId()).isEqualTo(secondCustomUser.getMemberId());
        assertThat(firstCustomUser.getUsername()).isEqualTo(secondCustomUser.getUsername());
        assertThat(firstCustomUser.getProvider()).isEqualTo(secondCustomUser.getProvider());
        
        // 멤버 서비스는 두 번 호출되어야 함
        verify(memberReadService, times(2)).getById(1L);
    }
}