package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.model.dto.member.SignUpRequest;
import com.mylog.model.dto.member.UpdateMemberRequest;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private S3Service s3Service;

    private SignUpRequest signUpRequest;
    private UpdateMemberRequest updateRequest;
    private CustomUser customUser;
    private MockMultipartFile file;
    private Member member;
    private Long memberId = 123L;
    private String email = "test@example.com";
    private String memberName = "Test User";
    private String password = "password123";
    private String nickname = "testNickname";
    private String cryptedPassword = "encodedPassword";
    private String profileImg = "https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/f47ac10b-58cc-4372-a567-0e02b2c3d479_test.jpg";
    private String newProfileImg = "https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/f47ac10b-58cc-4372-a567-0e02b2c3d479_new.jpg";
    private String basicImageUrl = "https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/f47ac10b-58cc-4372-a567-0e02b2c3d479_basic.jpg";


    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // 테스트용 객체 초기화
        signUpRequest = new SignUpRequest();
        signUpRequest.setEmail(email);
        signUpRequest.setMemberName(memberName);
        signUpRequest.setPassword(password);

        updateRequest = new UpdateMemberRequest();
        updateRequest.setPassword("updatedPassword");
        updateRequest.setMemberName("updatedMemberName");
        updateRequest.setNickname("updatedNickname");
        updateRequest.setBio("updatedBio");

        member = Member.builder()
            .id(memberId)
            .email(email)
            .memberName(memberName)
            .nickname(nickname)
            .password(cryptedPassword)
            .nickname(email)
            .provider(OauthProvider.LOCAL)
            .providerId(email + OauthProvider.LOCAL)
            .profileImg(profileImg)
            .build();

        Collection<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        customUser = new CustomUser(member, authorities);

        file = new MockMultipartFile("file", "new.jpg", "image/jpeg", "new image content".getBytes());

        //@Value 모킹
        var bucketNameField = MemberService.class.getDeclaredField("basicImageUrl");
        bucketNameField.setAccessible(true);
        bucketNameField.set(memberService, basicImageUrl);
    }

    @Test
    void 회원가입_성공_멤버저장() {
        // Given
        when(memberRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(cryptedPassword);
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        doNothing().when(categoryService).createCategory(email);
        // When
        memberService.saveMember(signUpRequest);

        // Then
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(memberRepository).save(memberCaptor.capture());

        Member capturedMember = memberCaptor.getValue();
        assertThat(capturedMember.getEmail()).isEqualTo(email);
        assertThat(capturedMember.getMemberName()).isEqualTo(memberName);
        assertThat(capturedMember.getPassword()).isEqualTo(cryptedPassword);
        assertThat(capturedMember.getNickname()).isEqualTo(email);
        assertThat(capturedMember.getProvider()).isEqualTo(OauthProvider.LOCAL);
        assertThat(capturedMember.getProviderId()).isEqualTo(email + OauthProvider.LOCAL);
        assertThat(capturedMember.getProfileImg()).isEqualTo(basicImageUrl);
    }

    @Test
    void 회원가입_이메일중복_예외발생() {
        // Given
        when(memberRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberService.saveMember(signUpRequest))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage("이미 존재하는 이메일입니다.");
        verify(memberRepository).existsByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 회원가입_비밀번호암호화실패_예외발생() {
        // Given
        when(memberRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> memberService.saveMember(signUpRequest))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage("비밀번호 암호화를 실패했습니다.");
        verify(memberRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 사용자_정보조회_성공_멤버반환() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // When
        Member result = memberService.getMember(customUser);

        // Then
        assertThat(result).isEqualTo(member);
        assertThat(result.getId()).isEqualTo(memberId);
        assertThat(result.getEmail()).isEqualTo(email);
        verify(memberRepository).findById(memberId);
    }

    @Test
    void 사용자_정보조회_멤버없음_예외발생() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.getMember(customUser))
            .isInstanceOf(CMissingDataException.class);
        verify(memberRepository).findById(memberId);
    }

    @Test
    void 사용자_정보수정_성공_다른이미지() throws IOException {
        // Given
        when(memberRepository.existsByNickname(updateRequest.getNickname())).thenReturn(false);
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));
        when(s3Service.upload(file)).thenReturn(Optional.of(newProfileImg));
        doNothing().when(s3Service).deleteImage(profileImg);

        // When
        memberService.updateMember(updateRequest, customUser, file);

        // Then
        verify(memberRepository).existsByNickname(updateRequest.getNickname());
        verify(memberRepository).findById(customUser.getMemberId());
        verify(s3Service).upload(file);
        verify(s3Service).deleteImage(profileImg);
        assertThat(member.getProfileImg()).isEqualTo(newProfileImg);
    }



    @Test
    void 사용자_정보수정_성공_동일이미지() throws IOException {
        // Given
        file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "same image content".getBytes());
        when(memberRepository.existsByNickname(updateRequest.getNickname())).thenReturn(false);
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));

        //test.jpg

        // When
        memberService.updateMember(updateRequest, customUser, file);

        // Then
        verify(memberRepository).existsByNickname(updateRequest.getNickname());
        verify(memberRepository).findById(customUser.getMemberId());
        verify(s3Service, never()).upload(any());
    }

    @Test
    void 사용자_정보수정_성공_기본이미지_삭제방지() throws IOException {
        // Given
        file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "same image content".getBytes());
        when(memberRepository.existsByNickname(updateRequest.getNickname())).thenReturn(false);
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));


        // When
        memberService.updateMember(updateRequest, customUser, file);

        // Then
        verify(memberRepository).existsByNickname(updateRequest.getNickname());
        verify(memberRepository).findById(customUser.getMemberId());
        verify(s3Service, never()).upload(any());
    }

    @Test
    void 사용자_정보수정_닉네임중복_예외발생() throws IOException {
        // Given
        when(memberRepository.existsByNickname(updateRequest.getNickname())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberService.updateMember(updateRequest, customUser, file))
            .isInstanceOf(CInvalidDataException.class)
            .hasMessage("중복되는 닉네임 입니다.");
        verify(memberRepository).existsByNickname(updateRequest.getNickname());
        verify(memberRepository, never()).findById(anyLong());
        verify(s3Service, never()).upload(any());
    }

    @Test
    void 사용자_정보수정_멤버없음_예외발생() throws IOException {
        // Given
        when(memberRepository.existsByNickname(updateRequest.getNickname())).thenReturn(false);
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.updateMember(updateRequest, customUser, file))
            .isInstanceOf(CMissingDataException.class);
        verify(memberRepository).existsByNickname(updateRequest.getNickname());
        verify(memberRepository).findById(customUser.getMemberId());
        verify(s3Service, never()).upload(any());
    }

    @Test
    void 사용자_정보수정_S3업로드실패_예외발생() throws IOException {
        // Given
        when(memberRepository.existsByNickname(updateRequest.getNickname())).thenReturn(false);
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));
        when(s3Service.upload(file)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.updateMember(updateRequest, customUser, file))
            .isInstanceOf(CMissingDataException.class);
        verify(memberRepository).existsByNickname(updateRequest.getNickname());
        verify(memberRepository).findById(customUser.getMemberId());
        verify(s3Service).upload(file);
    }

    @Test
    void 사용자_정보수정_IOException발생_예외전파() throws IOException {
        // Given
        when(memberRepository.existsByNickname(updateRequest.getNickname())).thenReturn(false);
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));
        when(s3Service.upload(file)).thenThrow(new IOException("S3 upload failed"));

        // When & Then
        assertThatThrownBy(() -> memberService.updateMember(updateRequest, customUser, file))
            .isInstanceOf(IOException.class)
            .hasMessage("S3 upload failed");
        verify(memberRepository).existsByNickname(updateRequest.getNickname());
        verify(memberRepository).findById(customUser.getMemberId());
        verify(s3Service).upload(file);
    }

    @Test
    void 사용자_정보삭제_커스텀이미지_성공() {
        // Given

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        doNothing().when(s3Service).deleteImage(member.getProfileImg());
        doNothing().when(memberRepository).deleteById(memberId);

        // When
        memberService.deleteMember(customUser);

        // Then
        verify(memberRepository).findById(memberId);
        verify(s3Service).deleteImage(member.getProfileImg());
        verify(memberRepository).deleteById(memberId);
    }

    @Test
    void 사용자_정보삭제_기본이미지_성공() {
        // Given
        member.setProfileImg(basicImageUrl);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        doNothing().when(memberRepository).deleteById(memberId);

        // When
        memberService.deleteMember(customUser);

        // Then
        verify(memberRepository).findById(memberId);
        verify(s3Service, never()).deleteImage(member.getProfileImg());
        verify(memberRepository).deleteById(memberId);
    }



    @Test
    void 사용자_정보삭제_멤버없음_예외발생() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.deleteMember(customUser))
            .isInstanceOf(CMissingDataException.class);
        verify(memberRepository).findById(memberId);
        verify(s3Service, never()).deleteImage(anyString());
        verify(memberRepository, never()).deleteById(anyLong());
    }
}