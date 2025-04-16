package com.mylog.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.dto.SignUpRequest;
import com.mylog.dto.UpdateMemberRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LocalMemberServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "Test User";
    private static final String TEST_PASSWORD = "Test@1234";
    private static final String ENCRYPTED_PASSWORD = "encryptedPassword";
    private static final String PASSWORD_ENCRYPTION_ERROR = "비밀번호 암호화를 실패했습니다.";
    private static final String UPDATED_NAME = "Updated User";
    private static final String UPDATED_NICKNAME = "updatedNick";
    private static final String UPDATED_BIO = "Updated Bio";
    private static final String UPDATED_PROFILE_IMAGE = "updated_image.jpg";

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private LocalMemberService localMemberService;

    @Test
    void 회원가입_성공() {
        // Given
        SignUpRequest request = createSignUpRequest();
        when(passwordEncoder.encode(request.getPassword())).thenReturn(ENCRYPTED_PASSWORD);

        // When
        localMemberService.saveMember(request);

        // Then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());

        Member savedMember = captor.getValue();
        assertMemberFields(savedMember);
    }

    @Test
    void 회원가입_실패_비밀번호암호화_실패() {
        // Given
        SignUpRequest request = createSignUpRequest();
        when(passwordEncoder.encode(request.getPassword())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> localMemberService.saveMember(request))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage(PASSWORD_ENCRYPTION_ERROR);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void 회원생성_이메일중복_실패() {
        // Given
        SignUpRequest request = createSignUpRequest();
        when(memberRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> localMemberService.saveMember(request))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage("이미 존재하는 이메일입니다.");
    }


    @Test
    void 회원정보_조회_성공() {
        // Given
        Member member = createMember();
        CustomUser customUser = new CustomUser(member, Collections.emptyList());
        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(member));

        // When
        Member result = localMemberService.getMember(customUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
        verify(memberRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void 회원정보_조회_실패_회원없음() {
        // Given
        Member member = createMember();
        CustomUser customUser = new CustomUser(member, Collections.emptyList());
        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> localMemberService.getMember(customUser))
            .isInstanceOf(CMissingDataException.class);
    }



    @Test
    void 회원_삭제_성공() {
        // Given
        Member member = createMember();
        CustomUser customUser = new CustomUser(member, Collections.emptyList());

        // When
        localMemberService.deleteMember(customUser);

        // Then
        verify(memberRepository).deleteByEmail(TEST_EMAIL);
    }


    private Member createMember() {
        return Member.builder()
            .email(TEST_EMAIL)
            .memberName(TEST_USERNAME)
            .password(ENCRYPTED_PASSWORD)
            .nickname(TEST_USERNAME)
            .provider(OauthProvider.LOCAL)
            .providerId(TEST_EMAIL)
            .build();
    }

    private SignUpRequest createSignUpRequest() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail(TEST_EMAIL);
        request.setMemberName(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);
        return request;
    }

    private UpdateMemberRequest createUpdateRequest() {
        return UpdateMemberRequest.builder()
            .email(TEST_EMAIL)
            .memberName(UPDATED_NAME)
            .nickname(UPDATED_NICKNAME)
            .bio(UPDATED_BIO)
            .build();
    }

    private void assertMemberFields(Member savedMember) {
        assertThat(savedMember.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(savedMember.getMemberName()).isEqualTo(TEST_USERNAME);
        assertThat(savedMember.getPassword()).isEqualTo(ENCRYPTED_PASSWORD);
        assertThat(savedMember.getNickname()).isEqualTo(TEST_USERNAME);
        assertThat(savedMember.getProvider()).isEqualTo(OauthProvider.LOCAL);
        assertThat(savedMember.getProviderId()).isEqualTo(TEST_EMAIL);
    }
}