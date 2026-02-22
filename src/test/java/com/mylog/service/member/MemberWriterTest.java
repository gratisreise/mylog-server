package com.mylog.service.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.member.service.MemberWriter;
import com.mylog.common.exception.CDuplicatedException;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.common.security.CustomUser;
import com.mylog.domain.auth.dto.request.SignUpRequest;
import com.mylog.domain.member.dto.UpdateMemberRequest;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.repository.MemberRepository;
import com.mylog.external.s3.S3Provider;
import com.mylog.domain.category.service.CategoryWriter;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MemberWriterTest {

    @InjectMocks
    private MemberWriter memberWriter;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private S3Provider s3Provider;

    @Mock
    private CategoryWriter categoryWriter;

    @Mock
    private MemberReader memberReader;

    private Member member;
    private CustomUser customUser;
    private final String BASIC_IMAGE_URL = "https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/5162d5b3-266b-4aae-bc16-d7f10fc4b2f1_basic.png";

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testuser")
                .password("encodedPassword")
                .profileImg("https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/5162d5b3-266b-4aae-bc16-d7f10fc4b2f1_default.jpg")
                .build();
        customUser = new CustomUser(member, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        ReflectionTestUtils.setField(memberWriter, "basicImageUrl", BASIC_IMAGE_URL);
    }

    @Test
    @DisplayName("회원 가입 성공")
    void saveMember_성공() {
        // Given
        SignUpRequest request = new SignUpRequest("test@example.com", "password", "testuser");
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doNothing().when(categoryWriter).createCategory(any());

        // When
        memberWriter.saveMember(request);

        // Then
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(categoryWriter, times(1)).createCategory(any());
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 중복")
    void saveMember_실패_이메일_중복() {
        // Given
        SignUpRequest request = new SignUpRequest("test@example.com", "password", "testuser");
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberWriter.saveMember(request))
                .isInstanceOf(CDuplicatedException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("회원 가입 실패 - 비밀번호 암호화 실패")
    void saveMember_실패_암호화_실패() {
        // Given
        SignUpRequest request = new SignUpRequest("test@example.com", "password", "testuser");
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> memberWriter.saveMember(request))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("비밀번호 암호화를 실패했습니다.");
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateMember_성공() throws IOException {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword123!", "newName", "newNickname", "newBio", null);
        MockMultipartFile file = new MockMultipartFile("file", "new_image.jpg", "image/jpeg", "image data".getBytes());
        String originalProfileImg = member.getProfileImg();

        when(memberReader.getById(1L)).thenReturn(member);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(s3Provider.upload(any(MultipartFile.class))).thenReturn("http\\://example.com/new.jpg");

        // When
        memberWriter.updateMember(request, customUser, file);

        // Then
        verify(passwordEncoder, times(1)).encode("newPassword123!");
        verify(s3Provider, times(1)).upload(eq(file));
        verify(s3Provider, times(1)).deleteImage(originalProfileImg);
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 이미지 변경 없음")
    void updateMember_성공_이미지_변경_없음() throws IOException {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword123!", "newName", "newNickname", "newBio", null);
        // The original file name from the uploaded file is the same as the filename in the existing URL
        MockMultipartFile file = new MockMultipartFile("file", "default.jpg", "image/jpeg", "image data".getBytes());
        when(memberReader.getById(1L)).thenReturn(member);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        // When
        memberWriter.updateMember(request, customUser, file);

        // Then
        verify(passwordEncoder, times(1)).encode("newPassword123!");
        verify(s3Provider, never()).upload(any(MultipartFile.class));
        verify(s3Provider, never()).deleteImage(anyString());
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 기본 이미지에서 변경")
    void updateMember_성공_기본이미지에서_변경() throws IOException {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword123!", "newName", "newNickname", "newBio", null);
        MockMultipartFile file = new MockMultipartFile("file", "new_image.jpg", "image/jpeg", "image data".getBytes());
        when(memberReader.getById(1L)).thenReturn(member);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(s3Provider.upload(any(MultipartFile.class))).thenReturn("http\\://example.com/new.jpg");

        // When
        memberWriter.updateMember(request, customUser, file);

        // Then
        verify(passwordEncoder, times(1)).encode("newPassword123!");
        verify(s3Provider, times(1)).upload(file);
        verify(s3Provider, never()).deleteImage(anyString());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 닉네임 중복")
    void updateMember_실패_닉네임_중복() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword123!", "newName", "newNickname", "newBio", null);
        MockMultipartFile file = new MockMultipartFile("file", "new_image.jpg", "image/jpeg", "image data".getBytes());
        when(memberReader.getById(1L)).thenReturn(member);
        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberWriter.updateMember(request, customUser, file))
                .isInstanceOf(CDuplicatedException.class)
                .hasMessage("중복되는 닉네임 입니다.");
    }


    @Test
    @DisplayName("회원 삭제 성공")
    void deleteMember_성공() {
        // Given
        when(memberReader.getById(1L)).thenReturn(member);
        doNothing().when(s3Provider).deleteImage(anyString());

        // When
        memberWriter.deleteMember(customUser);

        // Then
        verify(memberRepository, times(1)).deleteById(1L);
        verify(s3Provider, times(1)).deleteImage(member.getProfileImg());
    }

    @Test
    @DisplayName("회원 삭제 성공 - 기본 이미지 사용자")
    void deleteMember_성공_기본이미지_사용자() {
        // Given
        when(memberReader.getById(1L)).thenReturn(member);

        // When
        memberWriter.deleteMember(customUser);

        // Then
        verify(memberRepository, times(1)).deleteById(1L);
        verify(s3Provider, never()).deleteImage(anyString());
    }
}