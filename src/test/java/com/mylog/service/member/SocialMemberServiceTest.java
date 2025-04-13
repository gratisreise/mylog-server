package com.mylog.service.member;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mylog.dto.UpdateMemberRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SocialMemberServiceTest {

    @InjectMocks
    private SocialMemberService socialMemberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Member testMember;
    private CustomUser customUser;
    private static final String TEST_NICKNAME = "테스트닉네임";

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1L)
            .memberName("테스트유저")
            .nickname(TEST_NICKNAME)
            .provider(OauthProvider.KAKAO)
            .providerId("12345")
            .build();

        customUser = new CustomUser("1", Collections.emptyList());
    }

    @Test
    void 회원_조회_성공() {
        // given
        when(memberRepository.findById(1L))
            .thenReturn(Optional.of(testMember));

        // when
        Member result = socialMemberService.getMember(customUser);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo(TEST_NICKNAME);
        assertThat(result.getProvider()).isEqualTo(OauthProvider.KAKAO);

        verify(memberRepository).findById(1L);
    }

    @Test
    void 회원_조회_실패_회원정보없음() {
        // given
        when(memberRepository.findById(1L))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> socialMemberService.getMember(customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findById(1L);
    }

    @Test
    void 회원정보_수정_성공() {
        // given
        UpdateMemberRequest request = new UpdateMemberRequest();
        request.setNickname("새로운닉네임");
        request.setMemberName("새이름");

        when(memberRepository.findById(1L))
            .thenReturn(Optional.of(testMember));

        // when & then
        assertThatCode(() -> socialMemberService.updateMember(request, customUser))
            .doesNotThrowAnyException();

        verify(memberRepository).findById(1L);
        assertThat(testMember.getNickname()).isEqualTo("새로운닉네임");
        assertThat(testMember.getMemberName()).isEqualTo("새이름");
    }

    @Test
    void 회원정보_수정_실패_회원정보없음() {
        // given
        UpdateMemberRequest request = new UpdateMemberRequest();
        request.setNickname("새로운닉네임");
        request.setMemberName("새이름");

        when(memberRepository.findById(1L))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> socialMemberService.updateMember(request, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findById(1L);
    }

    @Test
    void 회원_삭제_성공() {
        // given
        doNothing().when(memberRepository).deleteById(1L);

        // when & then
        assertThatCode(() -> socialMemberService.deleteMember(customUser))
            .doesNotThrowAnyException();

        verify(memberRepository).deleteById(1L);
    }


}