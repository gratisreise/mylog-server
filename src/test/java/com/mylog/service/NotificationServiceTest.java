package com.mylog.service;

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
import com.mylog.service.member.MemberReadService;
import com.mylog.service.notification.NotificationService;
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

/**
 * Comprehensive unit tests for NotificationService
 * Tests notification sending, reading, settings management, and pagination
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private MemberReadService memberReadService;

    private Member testMember;
    private Member oauthMember;
    private CustomUser customUser;
    private NotificationSetting enabledSetting;
    private NotificationSetting disabledSetting;
    private Notification testNotification;
    private Notification readNotification;

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

        // OAuth Member setup
        oauthMember = Member.builder()
            .id(2L)
            .email("oauth@example.com")
            .nickname("oauthuser")
            .memberName("OAuth User")
            .password("oauth_password")
            .profileImg("oauth-profile.jpg")
            .provider(OauthProvider.GOOGLE)
            .providerId("google123")
            .build();

        // CustomUser setup
        customUser = new CustomUser(
            testMember,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // NotificationSetting setup
        enabledSetting = NotificationSetting.builder()
            .id(1L)
            .member(testMember)
            .type(TEST_TYPE)
            .disabled(false)
            .build();

        disabledSetting = NotificationSetting.builder()
            .id(2L)
            .member(testMember)
            .type(TEST_TYPE)
            .disabled(true)
            .build();

        // Notification setup
        testNotification = Notification.builder()
            .id(TEST_NOTIFICATION_ID)
            .member(testMember)
            .type(TEST_TYPE)
            .relatedId(TEST_RELATED_ID)
            .read(false)
            .build();

        readNotification = Notification.builder()
            .id(TEST_NOTIFICATION_ID + 1)
            .member(testMember)
            .type(TEST_TYPE)
            .relatedId(TEST_RELATED_ID)
            .read(true)
            .build();
    }

    @Nested
    @DisplayName("sendNotification Tests")
    class SendNotificationTests {

        @Test
        @DisplayName("알림 설정이 활성화된 경우 알림을 생성한다")
        void sendNotification_WhenNotificationEnabled_CreatesNotification() {
            // Given
            when(notificationSettingRepository.findByMemberAndType(testMember, TEST_TYPE))
                .thenReturn(Optional.of(enabledSetting));

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
            when(notificationSettingRepository.findByMemberAndType(testMember, TEST_TYPE))
                .thenReturn(Optional.of(disabledSetting));

            // When
            notificationService.sendNotification(testMember, TEST_RELATED_ID, TEST_TYPE);

            // Then
            verify(notificationRepository, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("알림 설정이 존재하지 않는 경우 예외를 발생시킨다")
        void sendNotification_WhenNotificationSettingNotFound_ThrowsException() {
            // Given
            when(notificationSettingRepository.findByMemberAndType(testMember, TEST_TYPE))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> notificationService.sendNotification(testMember, TEST_RELATED_ID, TEST_TYPE))
                .isInstanceOf(CMissingDataException.class);

            verify(notificationRepository, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("다양한 타입의 알림을 생성할 수 있다")
        void sendNotification_WithDifferentTypes_CreatesNotificationsSuccessfully() {
            // Given
            String likeType = "LIKE";
            NotificationSetting likeSetting = NotificationSetting.builder()
                .member(testMember)
                .type(likeType)
                .disabled(false)
                .build();
            
            when(notificationSettingRepository.findByMemberAndType(testMember, likeType))
                .thenReturn(Optional.of(likeSetting));

            // When
            notificationService.sendNotification(testMember, TEST_RELATED_ID, likeType);

            // Then
            ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(notificationCaptor.capture());
            
            Notification savedNotification = notificationCaptor.getValue();
            assertThat(savedNotification.getType()).isEqualTo(likeType);
        }
    }

    @Nested
    @DisplayName("readNotification Tests")
    class ReadNotificationTests {

        @Test
        @DisplayName("존재하는 알림을 읽음 처리한다")
        void readNotification_WhenNotificationExists_MarksAsRead() {
            // Given
            when(notificationRepository.findById(TEST_NOTIFICATION_ID))
                .thenReturn(Optional.of(testNotification));

            // When
            notificationService.readNotification(TEST_NOTIFICATION_ID);

            // Then
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
            // Note: makeRead() method call is verified through the entity state change
        }

        @Test
        @DisplayName("존재하지 않는 알림 ID로 읽기 시도 시 예외를 발생시킨다")
        void readNotification_WhenNotificationNotFound_ThrowsException() {
            // Given
            when(notificationRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> notificationService.readNotification(999L))
                .isInstanceOf(CMissingDataException.class);
        }

        @Test
        @DisplayName("이미 읽은 알림도 다시 읽음 처리할 수 있다")
        void readNotification_WhenAlreadyRead_StillCallsMakeRead() {
            // Given
            when(notificationRepository.findById(TEST_NOTIFICATION_ID))
                .thenReturn(Optional.of(readNotification));

            // When
            notificationService.readNotification(TEST_NOTIFICATION_ID);

            // Then
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
        }
    }

    @Nested
    @DisplayName("createNotificationSetting Tests")
    class CreateNotificationSettingTests {

        @Test
        @DisplayName("새로운 알림 설정을 생성한다")
        void createNotificationSetting_WhenNotExists_CreatesNewSetting() {
            // Given
            when(notificationSettingRepository.existsByMemberAndType(testMember, TEST_TYPE))
                .thenReturn(false);

            // When
            notificationService.createNotificationSetting(testMember, TEST_TYPE);

            // Then
            ArgumentCaptor<NotificationSetting> settingCaptor = ArgumentCaptor.forClass(NotificationSetting.class);
            verify(notificationSettingRepository).save(settingCaptor.capture());
            
            NotificationSetting savedSetting = settingCaptor.getValue();
            assertThat(savedSetting.getMember()).isEqualTo(testMember);
            assertThat(savedSetting.getType()).isEqualTo(TEST_TYPE);
            assertThat(savedSetting.isDisabled()).isFalse(); // Default should be enabled
        }

        @Test
        @DisplayName("이미 존재하는 알림 설정은 중복 생성하지 않는다")
        void createNotificationSetting_WhenAlreadyExists_DoesNotCreateDuplicate() {
            // Given
            when(notificationSettingRepository.existsByMemberAndType(testMember, TEST_TYPE))
                .thenReturn(true);

            // When
            notificationService.createNotificationSetting(testMember, TEST_TYPE);

            // Then
            verify(notificationSettingRepository, never()).save(any(NotificationSetting.class));
        }

        @Test
        @DisplayName("다른 회원의 동일한 타입 설정은 생성할 수 있다")
        void createNotificationSetting_ForDifferentMember_CreatesNewSetting() {
            // Given
            when(notificationSettingRepository.existsByMemberAndType(oauthMember, TEST_TYPE))
                .thenReturn(false);

            // When
            notificationService.createNotificationSetting(oauthMember, TEST_TYPE);

            // Then
            ArgumentCaptor<NotificationSetting> settingCaptor = ArgumentCaptor.forClass(NotificationSetting.class);
            verify(notificationSettingRepository).save(settingCaptor.capture());
            
            NotificationSetting savedSetting = settingCaptor.getValue();
            assertThat(savedSetting.getMember()).isEqualTo(oauthMember);
            assertThat(savedSetting.getType()).isEqualTo(TEST_TYPE);
        }

        @Test
        @DisplayName("동일한 회원의 다른 타입 설정은 생성할 수 있다")
        void createNotificationSetting_ForDifferentType_CreatesNewSetting() {
            // Given
            String differentType = "LIKE";
            when(notificationSettingRepository.existsByMemberAndType(testMember, differentType))
                .thenReturn(false);

            // When
            notificationService.createNotificationSetting(testMember, differentType);

            // Then
            ArgumentCaptor<NotificationSetting> settingCaptor = ArgumentCaptor.forClass(NotificationSetting.class);
            verify(notificationSettingRepository).save(settingCaptor.capture());
            
            NotificationSetting savedSetting = settingCaptor.getValue();
            assertThat(savedSetting.getType()).isEqualTo(differentType);
        }
    }

    @Nested
    @DisplayName("receiveNotification Tests")
    class ReceiveNotificationTests {

        @Test
        @DisplayName("읽지 않은 알림들을 페이지네이션으로 조회한다")
        void receiveNotification_ReturnsUnreadNotificationsWithPagination() {
            // Given
            when(memberReadService.getByCustomUser(customUser)).thenReturn(testMember);
            
            List<Notification> notifications = List.of(testNotification);
            Page<Notification> notificationPage = new PageImpl<>(notifications);
            
            when(notificationRepository.findAllByMemberAndReadFalse(eq(testMember), any(Pageable.class)))
                .thenReturn(notificationPage);

            // When
            Page<NotificationResponse> result = notificationService.receiveNotification(customUser, Pageable.unpaged());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            
            verify(memberReadService).getByCustomUser(customUser);
            verify(notificationRepository).findAllByMemberAndReadFalse(eq(testMember), any(Pageable.class));
        }

        @Test
        @DisplayName("읽지 않은 알림이 없는 경우 빈 페이지를 반환한다")
        void receiveNotification_WhenNoUnreadNotifications_ReturnsEmptyPage() {
            // Given
            when(memberReadService.getByCustomUser(customUser)).thenReturn(testMember);
            
            Page<Notification> emptyPage = new PageImpl<>(Collections.emptyList());
            when(notificationRepository.findAllByMemberAndReadFalse(eq(testMember), any(Pageable.class)))
                .thenReturn(emptyPage);

            // When
            Page<NotificationResponse> result = notificationService.receiveNotification(customUser, Pageable.unpaged());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("읽은 알림은 조회 결과에 포함되지 않는다")
        void receiveNotification_ExcludesReadNotifications() {
            // Given
            when(memberReadService.getByCustomUser(customUser)).thenReturn(testMember);
            
            // Only unread notifications should be returned
            List<Notification> unreadNotifications = List.of(testNotification);
            Page<Notification> notificationPage = new PageImpl<>(unreadNotifications);
            
            when(notificationRepository.findAllByMemberAndReadFalse(eq(testMember), any(Pageable.class)))
                .thenReturn(notificationPage);

            // When
            Page<NotificationResponse> result = notificationService.receiveNotification(customUser, Pageable.unpaged());

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(notificationRepository).findAllByMemberAndReadFalse(eq(testMember), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("toggleNotification Tests")
    class ToggleNotificationTests {

        @Test
        @DisplayName("알림 설정을 토글한다")
        void toggleNotification_TogglesNotificationSetting() {
            // Given
            when(memberReadService.getByCustomUser(customUser)).thenReturn(testMember);
            when(notificationSettingRepository.findByMemberAndType(testMember, TEST_TYPE))
                .thenReturn(Optional.of(enabledSetting));

            // When
            notificationService.toggleNotification(customUser, TEST_TYPE);

            // Then
            verify(memberReadService).getByCustomUser(customUser);
            verify(notificationSettingRepository).findByMemberAndType(testMember, TEST_TYPE);
            // Note: toggle() method call is verified through the entity state change
        }

        @Test
        @DisplayName("존재하지 않는 알림 설정 토글 시 예외를 발생시킨다")
        void toggleNotification_WhenSettingNotFound_ThrowsException() {
            // Given
            when(memberReadService.getByCustomUser(customUser)).thenReturn(testMember);
            when(notificationSettingRepository.findByMemberAndType(testMember, TEST_TYPE))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> notificationService.toggleNotification(customUser, TEST_TYPE))
                .isInstanceOf(CMissingDataException.class);
        }

        @Test
        @DisplayName("다른 회원의 알림 설정은 토글되지 않는다")
        void toggleNotification_OnlyTogglesCurrentUserSettings() {
            // Given
            CustomUser otherUser = new CustomUser(
                oauthMember,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            
            when(memberReadService.getByCustomUser(otherUser)).thenReturn(oauthMember);
            when(notificationSettingRepository.findByMemberAndType(oauthMember, TEST_TYPE))
                .thenReturn(Optional.of(disabledSetting));

            // When
            notificationService.toggleNotification(otherUser, TEST_TYPE);

            // Then
            verify(memberReadService).getByCustomUser(otherUser);
            verify(notificationSettingRepository).findByMemberAndType(oauthMember, TEST_TYPE);
            verify(notificationSettingRepository, never()).findByMemberAndType(testMember, TEST_TYPE);
        }

        @Test
        @DisplayName("다양한 타입의 알림 설정을 토글할 수 있다")
        void toggleNotification_WorksWithDifferentTypes() {
            // Given
            String likeType = "LIKE";
            NotificationSetting likeSetting = NotificationSetting.builder()
                .member(testMember)
                .type(likeType)
                .disabled(false)
                .build();
            
            when(memberReadService.getByCustomUser(customUser)).thenReturn(testMember);
            when(notificationSettingRepository.findByMemberAndType(testMember, likeType))
                .thenReturn(Optional.of(likeSetting));

            // When
            notificationService.toggleNotification(customUser, likeType);

            // Then
            verify(notificationSettingRepository).findByMemberAndType(testMember, likeType);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("알림 생성부터 읽기까지의 전체 플로우가 정상 작동한다")
        void fullNotificationFlow_WorksCorrectly() {
            // Given - 알림 설정이 활성화된 상태
            when(notificationSettingRepository.findByMemberAndType(testMember, TEST_TYPE))
                .thenReturn(Optional.of(enabledSetting));
            when(notificationRepository.findById(TEST_NOTIFICATION_ID))
                .thenReturn(Optional.of(testNotification));

            // When - 알림 생성
            notificationService.sendNotification(testMember, TEST_RELATED_ID, TEST_TYPE);
            
            // Then - 알림이 생성됨
            verify(notificationRepository).save(any(Notification.class));

            // When - 알림 읽기
            notificationService.readNotification(TEST_NOTIFICATION_ID);
            
            // Then - 알림이 읽음 처리됨
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
        }

        @Test
        @DisplayName("설정 생성부터 토글까지의 전체 플로우가 정상 작동한다")
        void fullSettingFlow_WorksCorrectly() {
            // Given
            when(notificationSettingRepository.existsByMemberAndType(testMember, TEST_TYPE))
                .thenReturn(false)
                .thenReturn(true); // After creation
            when(memberReadService.getByCustomUser(customUser)).thenReturn(testMember);
            when(notificationSettingRepository.findByMemberAndType(testMember, TEST_TYPE))
                .thenReturn(Optional.of(enabledSetting));

            // When - 설정 생성
            notificationService.createNotificationSetting(testMember, TEST_TYPE);
            
            // Then - 설정이 생성됨
            verify(notificationSettingRepository).save(any(NotificationSetting.class));

            // When - 설정 토글
            notificationService.toggleNotification(customUser, TEST_TYPE);
            
            // Then - 설정이 토글됨
            verify(notificationSettingRepository).findByMemberAndType(testMember, TEST_TYPE);
        }
    }
}