package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.exception.CDuplicatedException;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.member.SignUpRequest;
import com.mylog.model.dto.member.UpdateMemberRequest;
import com.mylog.model.entity.Member;
import com.mylog.repository.member.MemberRepository;
import com.mylog.service.category.CategoryWriteService;
import com.mylog.service.member.MemberReadService;
import com.mylog.service.member.MemberWriteService;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
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
class MemberWriteServiceTest {

    @InjectMocks
    private MemberWriteService memberWriteService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private S3Service s3Service;

    @Mock
    private CategoryWriteService categoryWriteService;

    @Mock
    private MemberReadService memberReadService;

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
        ReflectionTestUtils.setField(memberWriteService, "basicImageUrl", BASIC_IMAGE_URL);
    }

    @Test
    @DisplayName("회원 가입 성공")
    void saveMember_성공() {
        // Given
        SignUpRequest request = new SignUpRequest("test@example.com", "password", "testuser");
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doNothing().when(categoryWriteService).createCategory(anyString());

        // When
        memberWriteService.saveMember(request);

        // Then
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(categoryWriteService, times(1)).createCategory(request.email());
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 중복")
    void saveMember_실패_이메일_중복() {
        // Given
        SignUpRequest request = new SignUpRequest("test@example.com", "password", "testuser");
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberWriteService.saveMember(request))
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
        assertThatThrownBy(() -> memberWriteService.saveMember(request))
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

        when(memberReadService.getById(1L)).thenReturn(member);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(s3Service.upload(any(MultipartFile.class))).thenReturn(Optional.of("http://example.com/new.jpg"));

        // When
        memberWriteService.updateMember(request, customUser, file);

        // Then
        verify(passwordEncoder, times(1)).encode("newPassword123!");
        verify(s3Service, times(1)).upload(file);
        verify(s3Service, times(1)).deleteImage(originalProfileImg);
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 이미지 변경 없음")
    void updateMember_성공_이미지_변경_없음() throws IOException {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword123!", "newName", "newNickname", "newBio", null);
        // The original file name from the uploaded file is the same as the filename in the existing URL
        MockMultipartFile file = new MockMultipartFile("file", "default.jpg", "image/jpeg", "image data".getBytes());
        when(memberReadService.getById(1L)).thenReturn(member);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        // When
        memberWriteService.updateMember(request, customUser, file);

        // Then
        verify(passwordEncoder, times(1)).encode("newPassword123!");
        verify(s3Service, never()).upload(any(MultipartFile.class));
        verify(s3Service, never()).deleteImage(anyString());
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 기본 이미지에서 변경")
    void updateMember_성공_기본이미지에서_변경() throws IOException {
        // Given
        member.setProfileImg(BASIC_IMAGE_URL);
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword123!", "newName", "newNickname", "newBio", null);
        MockMultipartFile file = new MockMultipartFile("file", "new_image.jpg", "image/jpeg", "image data".getBytes());
        when(memberReadService.getById(1L)).thenReturn(member);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(s3Service.upload(any(MultipartFile.class))).thenReturn(Optional.of("http://example.com/new.jpg"));

        // When
        memberWriteService.updateMember(request, customUser, file);

        // Then
        verify(passwordEncoder, times(1)).encode("newPassword123!");
        verify(s3Service, times(1)).upload(file);
        verify(s3Service, never()).deleteImage(anyString());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 닉네임 중복")
    void updateMember_실패_닉네임_중복() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword123!", "newName", "newNickname", "newBio", null);
        MockMultipartFile file = new MockMultipartFile("file", "new_image.jpg", "image/jpeg", "image data".getBytes());
        when(memberReadService.getById(1L)).thenReturn(member);
        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberWriteService.updateMember(request, customUser, file))
                .isInstanceOf(CDuplicatedException.class)
                .hasMessage("중복되는 닉네임 입니다.");
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - S3 업로드 실패")
    void updateMember_실패_S3업로드_실패() throws IOException {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword123!", "newName", "newNickname", "newBio", null);
        MockMultipartFile file = new MockMultipartFile("file", "new_image.jpg", "image/jpeg", "image data".getBytes());
        when(memberReadService.getById(1L)).thenReturn(member);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(s3Service.upload(any(MultipartFile.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberWriteService.updateMember(request, customUser, file))
                .isInstanceOf(CMissingDataException.class);
    }

    @Test
    @DisplayName("회원 삭제 성공")
    void deleteMember_성공() {
        // Given
        when(memberReadService.getById(1L)).thenReturn(member);
        doNothing().when(s3Service).deleteImage(anyString());

        // When
        memberWriteService.deleteMember(customUser);

        // Then
        verify(memberRepository, times(1)).deleteById(1L);
        verify(s3Service, times(1)).deleteImage(member.getProfileImg());
    }

    @Test
    @DisplayName("회원 삭제 성공 - 기본 이미지 사용자")
    void deleteMember_성공_기본이미지_사용자() {
        // Given
        member.setProfileImg(BASIC_IMAGE_URL);
        when(memberReadService.getById(1L)).thenReturn(member);

        // When
        memberWriteService.deleteMember(customUser);

        // Then
        verify(memberRepository, times(1)).deleteById(1L);
        verify(s3Service, never()).deleteImage(anyString());
    }
}