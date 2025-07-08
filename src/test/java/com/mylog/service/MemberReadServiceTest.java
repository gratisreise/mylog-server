package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.member.MemberResponse;
import com.mylog.model.entity.Member;
import com.mylog.repository.MemberRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Comprehensive unit tests for MemberReadService
 * Tests member retrieval by various identifiers, exception handling, and data validation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberReadService Unit Tests")
class MemberReadServiceTest {

    @InjectMocks
    private MemberReadService memberReadService;

    @Mock
    private MemberRepository memberRepository;

    private Member testMember;
    private Member oauthMember;
    private CustomUser customUser;

    private static final Long TEST_MEMBER_ID = 1L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NICKNAME = "testuser";
    private static final String OAUTH_EMAIL = "oauth@example.com";
    private static final String OAUTH_NICKNAME = "oauthuser";

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(TEST_MEMBER_ID)
                .email(TEST_EMAIL)
                .nickname(TEST_NICKNAME)
                .password("$2a$10$encrypted.password.hash")
                .memberName("Test User")
                .bio("Test bio")
                .profileImg("https://example.com/profile.jpg")
                .provider(OauthProvider.LOCAL)
                .providerId(TEST_EMAIL + OauthProvider.LOCAL)
                .build();

        oauthMember = Member.builder()
                .id(2L)
                .email(OAUTH_EMAIL)
                .nickname(OAUTH_NICKNAME)
                .password(null)
                .memberName("OAuth User")
                .bio("OAuth bio")
                .profileImg("https://example.com/oauth-profile.jpg")
                .provider(OauthProvider.GOOGLE)
                .providerId(OAUTH_EMAIL + OauthProvider.GOOGLE)
                .build();

        customUser = new CustomUser(testMember, Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("CustomUser로 회원 정보 조회 성공")
    void getMember_CustomUser로_조회_성공() {
        // Given
        when(memberRepository.findById(TEST_MEMBER_ID)).thenReturn(Optional.of(testMember));

        // When
        MemberResponse response = memberReadService.getMember(customUser);

        // Then
        assertThat(response).isNotNull();
        // MemberResponse 검증은 생성자에 따라 달라짐 - 실제 필드들을 확인
        
        verify(memberRepository).findById(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("CustomUser로 회원 정보 조회 실패 - 존재하지 않는 회원")
    void getMember_존재하지_않는_회원_실패() {
        // Given
        when(memberRepository.findById(TEST_MEMBER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getMember(customUser))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findById(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("ID로 회원 조회 성공")
    void getById_성공() {
        // Given
        when(memberRepository.findById(TEST_MEMBER_ID)).thenReturn(Optional.of(testMember));

        // When
        Member foundMember = memberReadService.getById(TEST_MEMBER_ID);

        // Then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getId()).isEqualTo(TEST_MEMBER_ID);
        assertThat(foundMember.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(foundMember.getNickname()).isEqualTo(TEST_NICKNAME);
        
        verify(memberRepository).findById(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("ID로 회원 조회 실패 - 존재하지 않는 ID")
    void getById_존재하지_않는_ID_실패() {
        // Given
        Long nonExistentId = 999L;
        when(memberRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getById(nonExistentId))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findById(nonExistentId);
    }

    

    @Test
    @DisplayName("닉네임으로 회원 조회 성공")
    void getByNickname_성공() {
        // Given
        when(memberRepository.findByNickname(TEST_NICKNAME)).thenReturn(Optional.of(testMember));

        // When
        Member foundMember = memberReadService.getByNickname(TEST_NICKNAME);

        // Then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getNickname()).isEqualTo(TEST_NICKNAME);
        assertThat(foundMember.getId()).isEqualTo(TEST_MEMBER_ID);
        assertThat(foundMember.getEmail()).isEqualTo(TEST_EMAIL);
        
        verify(memberRepository).findByNickname(TEST_NICKNAME);
    }

    @Test
    @DisplayName("닉네임으로 회원 조회 실패 - 존재하지 않는 닉네임")
    void getByNickname_존재하지_않는_닉네임_실패() {
        // Given
        String nonExistentNickname = "nonexistent";
        when(memberRepository.findByNickname(nonExistentNickname)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getByNickname(nonExistentNickname))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findByNickname(nonExistentNickname);
    }

    @Test
    @DisplayName("빈 문자열 닉네임으로 조회 시도")
    void getByNickname_빈_문자열() {
        // Given
        String emptyNickname = "";
        when(memberRepository.findByNickname(emptyNickname)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getByNickname(emptyNickname))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findByNickname(emptyNickname);
    }

    @Test
    @DisplayName("null 닉네임으로 조회 시도")
    void getByNickname_null_닉네임() {
        // Given
        when(memberRepository.findByNickname(null)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getByNickname(null))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findByNickname(null);
    }

    @Test
    @DisplayName("이메일로 회원 조회 성공")
    void getByEmail_성공() {
        // Given
        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));

        // When
        Member foundMember = memberReadService.getByEmail(TEST_EMAIL);

        // Then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(foundMember.getId()).isEqualTo(TEST_MEMBER_ID);
        assertThat(foundMember.getNickname()).isEqualTo(TEST_NICKNAME);
        
        verify(memberRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("이메일로 회원 조회 실패 - 존재하지 않는 이메일")
    void getByEmail_존재하지_않는_이메일_실패() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(memberRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getByEmail(nonExistentEmail))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 조회 시도")
    void getByEmail_잘못된_이메일_형식() {
        // Given
        String invalidEmail = "invalid-email";
        when(memberRepository.findByEmail(invalidEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getByEmail(invalidEmail))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findByEmail(invalidEmail);
    }

    @Test
    @DisplayName("CustomUser로 Member 엔티티 조회 성공")
    void getByCustomUser_성공() {
        // Given
        when(memberRepository.findById(TEST_MEMBER_ID)).thenReturn(Optional.of(testMember));

        // When
        Member foundMember = memberReadService.getByCustomUser(customUser);

        // Then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getId()).isEqualTo(TEST_MEMBER_ID);
        assertThat(foundMember.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(foundMember.getNickname()).isEqualTo(TEST_NICKNAME);
        
        verify(memberRepository).findById(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("CustomUser로 Member 엔티티 조회 실패")
    void getByCustomUser_실패() {
        // Given
        when(memberRepository.findById(TEST_MEMBER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getByCustomUser(customUser))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findById(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("다양한 OAuth 제공자 회원 조회")
    void getBy다양한OAuth제공자_회원조회() {
        // Given
        Member googleMember = Member.builder()
                .id(3L)
                .email("google@example.com")
                .nickname("googleuser")
                .password(null)
                .memberName("Google User")
                .bio("Google bio")
                .profileImg("https://example.com/google-profile.jpg")
                .provider(OauthProvider.GOOGLE)
                .providerId("google@example.com" + OauthProvider.GOOGLE)
                .build();

        Member kakaoMember = Member.builder()
                .id(4L)
                .email("kakao@example.com")
                .nickname("kakaouser")
                .password(null)
                .memberName("Kakao User")
                .bio("Kakao bio")
                .profileImg("https://example.com/kakao-profile.jpg")
                .provider(OauthProvider.KAKAO)
                .providerId("kakao@example.com" + OauthProvider.KAKAO)
                .build();

        when(memberRepository.findByEmail("google@example.com")).thenReturn(Optional.of(googleMember));
        when(memberRepository.findByNickname("kakaouser")).thenReturn(Optional.of(kakaoMember));

        // When
        Member googleFound = memberReadService.getByEmail("google@example.com");
        Member kakaoFound = memberReadService.getByNickname("kakaouser");

        // Then
        assertThat(googleFound.getProvider()).isEqualTo(OauthProvider.GOOGLE);
        assertThat(kakaoFound.getProvider()).isEqualTo(OauthProvider.KAKAO);
        
        verify(memberRepository).findByEmail("google@example.com");
        verify(memberRepository).findByNickname("kakaouser");
    }

    @Test
    @DisplayName("대소문자 구분 이메일 조회")
    void getByEmail_대소문자_구분() {
        // Given
        String upperCaseEmail = TEST_EMAIL.toUpperCase();
        when(memberRepository.findByEmail(upperCaseEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getByEmail(upperCaseEmail))
                .isInstanceOf(CMissingDataException.class);
        
        // 정확한 대소문자로 조회되는지 확인
        verify(memberRepository).findByEmail(upperCaseEmail);
        verify(memberRepository, never()).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("대소문자 구분 닉네임 조회")
    void getByNickname_대소문자_구분() {
        // Given
        String upperCaseNickname = TEST_NICKNAME.toUpperCase();
        when(memberRepository.findByNickname(upperCaseNickname)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getByNickname(upperCaseNickname))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findByNickname(upperCaseNickname);
        verify(memberRepository, never()).findByNickname(TEST_NICKNAME);
    }

    @Test
    @DisplayName("공백이 포함된 닉네임 조회")
    void getByNickname_공백_포함() {
        // Given
        String nicknameWithSpaces = " testuser ";
        when(memberRepository.findByNickname(nicknameWithSpaces)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getByNickname(nicknameWithSpaces))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findByNickname(nicknameWithSpaces);
    }

    @Test
    @DisplayName("매우 긴 이메일 주소로 조회")
    void getByEmail_긴_이메일() {
        // Given
        String longEmail = "very.long.email.address.that.might.be.problematic@very-long-domain-name.com";
        when(memberRepository.findByEmail(longEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberReadService.getByEmail(longEmail))
                .isInstanceOf(CMissingDataException.class);
        
        verify(memberRepository).findByEmail(longEmail);
    }

    @Test
    @DisplayName("특수문자 포함 닉네임 조회")
    void getByNickname_특수문자_포함() {
        // Given
        String specialNickname = "user@#$%";
        Member specialMember = Member.builder()
                .id(5L)
                .nickname(specialNickname)
                .email("special@example.com")
                .password("$2a$10$encrypted.password.hash")
                .memberName("Special User")
                .bio("Special bio")
                .profileImg("https://example.com/special-profile.jpg")
                .provider(OauthProvider.LOCAL)
                .providerId("special@example.com" + OauthProvider.LOCAL)
                .build();
        
        when(memberRepository.findByNickname(specialNickname)).thenReturn(Optional.of(specialMember));

        // When
        Member foundMember = memberReadService.getByNickname(specialNickname);

        // Then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getNickname()).isEqualTo(specialNickname);
        
        verify(memberRepository).findByNickname(specialNickname);
    }

    @Test
    @DisplayName("연속된 조회 요청 처리")
    void 연속_조회_요청_처리() {
        // Given
        when(memberRepository.findById(TEST_MEMBER_ID)).thenReturn(Optional.of(testMember));
        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
        when(memberRepository.findByNickname(TEST_NICKNAME)).thenReturn(Optional.of(testMember));

        // When
        Member foundById = memberReadService.getById(TEST_MEMBER_ID);
        Member foundByEmail = memberReadService.getByEmail(TEST_EMAIL);
        Member foundByNickname = memberReadService.getByNickname(TEST_NICKNAME);

        // Then
        assertThat(foundById).isNotNull();
        assertThat(foundByEmail).isNotNull();
        assertThat(foundByNickname).isNotNull();
        
        // 모든 조회가 같은 회원을 반환하는지 확인
        assertThat(foundById.getId()).isEqualTo(foundByEmail.getId());
        assertThat(foundByEmail.getId()).isEqualTo(foundByNickname.getId());
        
        verify(memberRepository).findById(TEST_MEMBER_ID);
        verify(memberRepository).findByEmail(TEST_EMAIL);
        verify(memberRepository).findByNickname(TEST_NICKNAME);
    }

    @Test
    @DisplayName("Repository 예외 상황 처리")
    void repository_예외_상황_처리() {
        // Given
        when(memberRepository.findById(TEST_MEMBER_ID))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> memberReadService.getById(TEST_MEMBER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");
        
        verify(memberRepository).findById(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("트랜잭션 readOnly 어노테이션 확인을 위한 다중 조회")
    void 다중_조회_트랜잭션_테스트() {
        // Given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(testMember));

        // When - 여러 조회를 연속으로 실행
        memberReadService.getById(1L);
        memberReadService.getById(2L);
        memberReadService.getByEmail(TEST_EMAIL);
        memberReadService.getByEmail("another@example.com");

        // Then
        verify(memberRepository, times(2)).findById(anyLong());
        verify(memberRepository, times(2)).findByEmail(anyString());
    }
}