package com.mylog.service.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mylog.entity.Member;
import com.mylog.entity.Notification;
import com.mylog.entity.NotificationSetting;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.NotificationRepository;
import com.mylog.repository.NotificationSettingRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CommonNotificationServiceTest {

    @InjectMocks
    private CommonNotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    private Member testMember;
    private NotificationSetting testNotificationSetting;
    private Notification testNotification;
    private Pageable pageable;
    private static final Long TEST_ID = 1L;
    private static final String TEST_TYPE = "comment";

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1L)
            .memberName("테스트유저")
            .nickname("테스트닉네임")
            .build();

        testNotificationSetting = NotificationSetting.builder()
            .id(1L)
            .member(testMember)
            .type(TEST_TYPE)
            .disabled(false)
            .build();

        testNotification = Notification.builder()
            .id(1L)
            .member(testMember)
            .type(TEST_TYPE)
            .relatedId(1L)
            .read(false)
            .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void 알림_전송_성공() {
        // given
        String type = "comment";
        Long relatedId = 1L;

        NotificationSetting setting = NotificationSetting.builder()
            .member(testMember)
            .type(type)
            .disabled(false)
            .build();

        when(notificationSettingRepository.findByMemberAndType(testMember, type))
            .thenReturn(Optional.of(setting));

        when(notificationRepository.save(any(Notification.class)))
            .thenReturn(testNotification);

        // when
        notificationService.sendNotification(testMember, relatedId, type);

        // then
        verify(notificationSettingRepository).findByMemberAndType(testMember, type);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification capturedNotification = notificationCaptor.getValue();
        assertThat(capturedNotification.getMember()).isEqualTo(testMember);
        assertThat(capturedNotification.getType()).isEqualTo(type);
        assertThat(capturedNotification.getRelatedId()).isEqualTo(relatedId);
        assertThat(capturedNotification.isRead()).isFalse();
    }

    @Test
    void 알림_전송_실패_알림설정없음() {
        // given
        String type = "comment";
        Long relatedId = 1L;

        when(notificationSettingRepository.findByMemberAndType(testMember, type))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.sendNotification(testMember, relatedId, type))
            .isInstanceOf(CMissingDataException.class);

        verify(notificationSettingRepository).findByMemberAndType(testMember, type);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void 알림_전송_스킵_알림비활성화() {
        // given
        String type = "comment";
        Long relatedId = 1L;

        NotificationSetting setting = NotificationSetting.builder()
            .member(testMember)
            .type(type)
            .disabled(true)
            .build();

        when(notificationSettingRepository.findByMemberAndType(testMember, type))
            .thenReturn(Optional.of(setting));

        // when
        notificationService.sendNotification(testMember, relatedId, type);

        // then
        verify(notificationSettingRepository).findByMemberAndType(testMember, type);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void 알림_읽음_처리_성공() {
        // given
        Long notificationId = 1L;
        Notification notification = Notification.builder()
            .id(notificationId)
            .member(testMember)
            .type(TEST_TYPE)
            .relatedId(1L)
            .read(false)
            .build();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when
        notificationService.readNotification(notificationId);

        // then
        verify(notificationRepository).findById(notificationId);
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void 알림_읽음_처리_실패_알림없음() {
        // given
        Long notificationId = 999L;

        when(notificationRepository.findById(notificationId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.readNotification(notificationId))
            .isInstanceOf(CMissingDataException.class);

        verify(notificationRepository).findById(notificationId);
    }


    @Test
    void 알림설정_생성_성공() {
        // given
        String type = "comment";

        when(notificationSettingRepository.findByMemberAndType(testMember, type))
            .thenReturn(Optional.empty());

        ArgumentCaptor<NotificationSetting> settingCaptor = ArgumentCaptor.forClass(NotificationSetting.class);

        // when
        notificationService.createNotificationSetting(testMember, type);

        // then
        verify(notificationSettingRepository).findByMemberAndType(testMember, type);
        verify(notificationSettingRepository).save(settingCaptor.capture());

        NotificationSetting capturedSetting = settingCaptor.getValue();
        assertThat(capturedSetting.getMember()).isEqualTo(testMember);
        assertThat(capturedSetting.getType()).isEqualTo(type);
        assertThat(capturedSetting.isDisabled()).isFalse();
    }

    @Test
    void 알림설정_생성_스킵_이미존재() {
        // given
        String type = "comment";
        NotificationSetting existingSetting = NotificationSetting.builder()
            .member(testMember)
            .type(type)
            .disabled(false)
            .build();

        when(notificationSettingRepository.findByMemberAndType(testMember, type))
            .thenReturn(Optional.of(existingSetting));

        // when
        notificationService.createNotificationSetting(testMember, type);

        // then
        verify(notificationSettingRepository).findByMemberAndType(testMember, type);
        verify(notificationSettingRepository, never()).save(any(NotificationSetting.class));
    }

}