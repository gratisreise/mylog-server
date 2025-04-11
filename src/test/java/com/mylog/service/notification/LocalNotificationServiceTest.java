package com.mylog.service.notification;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.notification.NotificationResponse;
import com.mylog.entity.Member;
import com.mylog.entity.Notification;
import com.mylog.entity.NotificationSetting;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import com.mylog.repository.NotificationRepository;
import com.mylog.repository.NotificationSettingRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class LocalNotificationServiceTest {

    @InjectMocks
    private LocalNotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member testMember;
    private NotificationSetting testNotificationSetting;
    private Notification testNotification;
    private CustomUser customUser;
    private Pageable pageable;
    private static final String TEST_TYPE = "comment";

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1L)
            .memberName("테스트유저")
            .nickname("테스트닉네임")
            .email("test@test.com")
            .password("<PASSWORD>")
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

        customUser = new CustomUser(testMember, Collections.emptyList());
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void 알림_수신_성공() {
        // given
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        List<Notification> notifications = Collections.singletonList(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications, pageable, notifications.size());

        when(notificationRepository.findAllByMemberAndReadFalse(testMember, pageable))
            .thenReturn(notificationPage);

        // when
        Page<NotificationResponse> result = notificationService.receiveNotification(customUser, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent())
            .extracting("type", "notificationId")
            .containsExactly(
                tuple(TEST_TYPE, testNotification.getId())
            );

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(notificationRepository).findAllByMemberAndReadFalse(testMember, pageable);
    }

    @Test
    void 알림_수신_실패_회원정보없음() {
        // given
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.receiveNotification(customUser, pageable))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(notificationRepository, never()).findAllByMemberAndReadFalse(any(), any());
    }

    @Test
    void 알림_수신_성공_알림없음() {
        // given
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        Page<Notification> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(notificationRepository.findAllByMemberAndReadFalse(testMember, pageable))
            .thenReturn(emptyPage);

        // when
        Page<NotificationResponse> result = notificationService.receiveNotification(customUser, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(notificationRepository).findAllByMemberAndReadFalse(testMember, pageable);
    }

    @Test
    void 알림설정_토글_성공() {
        // given
        String type = "comment";
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));
        when(notificationSettingRepository.findByMemberAndType(testMember, type))
            .thenReturn(Optional.of(testNotificationSetting));

        // when
        notificationService.toggleNotification(customUser, type);

        // then
        assertThat(testNotificationSetting.isDisabled()).isTrue();
        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(notificationSettingRepository).findByMemberAndType(testMember, type);
    }

    @Test
    void 알림설정_토글_실패_회원정보없음() {
        // given
        String type = "comment";
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.toggleNotification(customUser, type))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(notificationSettingRepository, never()).findByMemberAndType(any(), any());
    }

    @Test
    void 알림설정_토글_실패_알림설정없음() {
        // given
        String type = "comment";
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));
        when(notificationSettingRepository.findByMemberAndType(testMember, type))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.toggleNotification(customUser, type))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(notificationSettingRepository).findByMemberAndType(testMember, type);
    }

}