package com.mylog.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.notification.NotificationResponse;
import com.mylog.model.entity.Member;
import com.mylog.model.entity.Notification;
import com.mylog.model.entity.NotificationSetting;
import com.mylog.repository.notification.NotificationRepository;
import com.mylog.repository.notificationsetting.NotificationSettingRepository;
import com.mylog.service.member.MemberReader;
import com.mylog.service.notificationsetting.NotificationSettingReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationReader notificationReader;

    @Mock
    private NotificationSettingReader notificationSettingReader;

    private Member testMember;
    private Notification testNotification;

    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_RELATED_ID = 10L;
    private static final Long TEST_NOTIFICATION_ID = 100L;
    private static final String TEST_TYPE = "COMMENT";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NICKNAME = "testuser";

    @BeforeEach
    void setUp() {
        // Test Member setup
        testMember = Member.builder()
            .id(TEST_MEMBER_ID)
            .email(TEST_EMAIL)
            .nickname(TEST_NICKNAME)
            .memberName("Test User")
            .password("password123")
            .profileImg("profile.jpg")
            .provider(OauthProvider.LOCAL)
            .providerId("test@example.com" + OauthProvider.LOCAL)
            .build();

        // Notification setup
        testNotification = Notification.builder()
            .id(TEST_NOTIFICATION_ID)
            .member(testMember)
            .type(TEST_TYPE)
            .relatedId(TEST_RELATED_ID)
            .read(false)
            .build();
    }

    @Nested
    @DisplayName("sendNotification Tests")
    class SendNotificationTests {

        @Test
        @DisplayName("알림 설정이 활성화된 경우 알림을 생성한다")
        void sendNotification_WhenNotificationEnabled_CreatesNotification() {
            // Given
            when(notificationSettingReader.isDisabled(testMember, TEST_TYPE)).thenReturn(false);

            // When
            notificationService.sendNotification(testMember, TEST_RELATED_ID, TEST_TYPE);

            // Then
            ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(notificationCaptor.capture());
            
            Notification savedNotification = notificationCaptor.getValue();
            assertThat(savedNotification.getMember()).isEqualTo(testMember);
            assertThat(savedNotification.getRelatedId()).isEqualTo(TEST_RELATED_ID);
            assertThat(savedNotification.getType()).isEqualTo(TEST_TYPE);
            assertThat(savedNotification.isRead()).isFalse();
        }

        @Test
        @DisplayName("알림 설정이 비활성화된 경우 알림을 생성하지 않는다")
        void sendNotification_WhenNotificationDisabled_DoesNotCreateNotification() {
            // Given
            when(notificationSettingReader.isDisabled(testMember, TEST_TYPE)).thenReturn(true);

            // When
            notificationService.sendNotification(testMember, TEST_RELATED_ID, TEST_TYPE);

            // Then
            verify(notificationRepository, never()).save(any(Notification.class));
        }
    }

    @Nested
    @DisplayName("readNotification Tests")
    class ReadNotificationTests {

        @Test
        @DisplayName("존재하는 알림을 읽음 처리한다")
        void readNotification_WhenNotificationExists_MarksAsRead() {
            // Given
            when(notificationReader.getById(TEST_NOTIFICATION_ID)).thenReturn(testNotification);

            // When
            notificationService.readNotification(TEST_NOTIFICATION_ID);

            // Then
            verify(notificationReader).getById(TEST_NOTIFICATION_ID);
        }

        @Test
        @DisplayName("존재하지 않는 알림 ID로 읽기 시도 시 예외를 발생시킨다")
        void readNotification_WhenNotificationNotFound_ThrowsException() {
            // Given
            Long nonExistId = 999L;
            when(notificationReader.getById(nonExistId)).thenThrow(CMissingDataException.class);

            // When & Then
            assertThatThrownBy(() -> notificationService.readNotification(nonExistId))
                .isInstanceOf(CMissingDataException.class);
            verify(notificationReader).getById(nonExistId);
        }

    }
}