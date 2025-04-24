package com.mylog.service;

import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.notification.NotificationResponse;
import com.mylog.entity.Member;
import com.mylog.entity.Notification;
import com.mylog.entity.NotificationSetting;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import com.mylog.repository.NotificationRepository;
import com.mylog.repository.NotificationSettingRepository;
import com.mylog.service.NotificationService;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private MemberRepository memberRepository;

    private CustomUser customUser;
    private Member member;
    private Notification notification;
    private NotificationSetting notificationSetting;
    private Pageable pageable;
    private Long memberId = 123L;
    private Long notificationId = 1L;
    private Long relatedId = 456L;
    private String type = "COMMENT";
    private String nickname = "testUser";

    @BeforeEach
    void setUp() {
        // 테스트용 객체 초기화
        member = Member.builder()
            .id(memberId)
            .nickname(nickname)
            .provider(OauthProvider.LOCAL)
            .password("<PASSWORD>")
            .build();

        Collection<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        customUser = new CustomUser(member, authorities);

        notification = Notification.builder()
            .id(notificationId)
            .member(member)
            .type(type)
            .relatedId(relatedId)
            .read(false)
            .build();

        notificationSetting = NotificationSetting.builder()
            .member(member)
            .type(type)
            .disabled(false)
            .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void 알림_전송_성공_저장완료() {
        // Given
        when(notificationSettingRepository.findByMemberAndType(member, type)).thenReturn(Optional.of(notificationSetting));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.sendNotification(member, relatedId, type);

        // Then
        verify(notificationSettingRepository).findByMemberAndType(member, type);
        verify(notificationRepository).save(any(Notification.class));
        assertThat(notification.getMember()).isEqualTo(member);
        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getRelatedId()).isEqualTo(relatedId);
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    void 알림_전송_설정꺼짐_저장안함() {
        // Given
        notificationSetting.setDisabled(true);
        when(notificationSettingRepository.findByMemberAndType(member, type)).thenReturn(Optional.of(notificationSetting));

        // When
        notificationService.sendNotification(member, relatedId, type);

        // Then
        verify(notificationSettingRepository).findByMemberAndType(member, type);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void 알림_전송_설정없음_예외발생() {
        // Given
        when(notificationSettingRepository.findByMemberAndType(member, type)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.sendNotification(member, relatedId, type))
            .isInstanceOf(CMissingDataException.class);
        verify(notificationSettingRepository).findByMemberAndType(member, type);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void 알림_읽기_성공_읽음처리() {
        // Given
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // When
        notificationService.readNotification(notificationId);

        // Then
        verify(notificationRepository).findById(notificationId);
        verify(notification).makeRead();
    }

    @Test
    void 알림_읽기_알림없음_예외발생() {
        // Given
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.readNotification(notificationId))
            .isInstanceOf(CMissingDataException.class);
        verify(notificationRepository).findById(notificationId);
        verify(notification, never()).makeRead();
    }

    @Test
    void 알림설정_생성_성공_저장완료() {
        // Given
        when(notificationSettingRepository.existsByMemberAndType(member, type)).thenReturn(false);
        when(notificationSettingRepository.save(any(NotificationSetting.class))).thenReturn(notificationSetting);

        // When
        notificationService.createNotificationSetting(member, type);

        // Then
        verify(notificationSettingRepository).existsByMemberAndType(member, type);
        verify(notificationSettingRepository).save(any(NotificationSetting.class));
        assertThat(notificationSetting.getMember()).isEqualTo(member);
        assertThat(notificationSetting.getType()).isEqualTo(type);
        assertThat(notificationSetting.isDisabled()).isFalse();
    }

    @Test
    void 알림설정_생성_이미존재_저장안함() {
        // Given
        when(notificationSettingRepository.existsByMemberAndType(member, type)).thenReturn(true);

        // When
        notificationService.createNotificationSetting(member, type);

        // Then
        verify(notificationSettingRepository).existsByMemberAndType(member, type);
        verify(notificationSettingRepository, never()).save(any());
    }

    @Test
    void 알림_수신_성공_알림목록반환() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        Page<Notification> notificationPage = new PageImpl<>(List.of(notification), pageable, 1);
        when(notificationRepository.findAllByMemberAndReadFalse(member, pageable)).thenReturn(notificationPage);

        // When
        Page<NotificationResponse> result = notificationService.receiveNotification(customUser, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNotificationId()).isEqualTo(notificationId);
        assertThat(result.getContent().get(0).getType()).isEqualTo(type);
        assertThat(result.getContent().get(0).getArticleId()).isEqualTo(notification.getRelatedId());
        verify(memberRepository).findById(memberId);
        verify(notificationRepository).findAllByMemberAndReadFalse(member, pageable);
    }

    @Test
    void 알림_수신_멤버없음_예외발생() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.receiveNotification(customUser, pageable))
            .isInstanceOf(CMissingDataException.class);
        verify(memberRepository).findById(memberId);
        verify(notificationRepository, never()).findAllByMemberAndReadFalse(any(), any());
    }

    @Test
    void 알림_토글_성공_설정변경() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(notificationSettingRepository.findByMemberAndType(member, type)).thenReturn(Optional.of(notificationSetting));

        // When
        notificationService.toggleNotification(customUser, type);

        // Then
        verify(memberRepository).findById(memberId);
        verify(notificationSettingRepository).findByMemberAndType(member, type);
        verify(notificationSetting).toggle();
    }

    @Test
    void 알림_토글_멤버없음_예외발생() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.toggleNotification(customUser, type))
            .isInstanceOf(CMissingDataException.class);
        verify(memberRepository).findById(memberId);
        verify(notificationSettingRepository, never()).findByMemberAndType(any(), any());
    }

    @Test
    void 알림_토글_설정없음_예외발생() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(notificationSettingRepository.findByMemberAndType(member, type)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.toggleNotification(customUser, type))
            .isInstanceOf(CMissingDataException.class);
        verify(memberRepository).findById(memberId);
        verify(notificationSettingRepository).findByMemberAndType(member, type);
    }
}