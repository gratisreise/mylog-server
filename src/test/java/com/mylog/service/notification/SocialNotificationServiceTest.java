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
class SocialNotificationServiceTest {

    @InjectMocks
    private SocialNotificationService notificationService;

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
            .type("comment")
            .disabled(false)
            .build();

        testNotification = Notification.builder()
            .id(1L)
            .member(testMember)
            .type("comment")
            .relatedId(1L)
            .read(false)
            .build();

        customUser = new CustomUser("1", Collections.emptyList());
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void 알림_수신_성공() {
        // given
        List<Notification> notifications = Collections.singletonList(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications, pageable, notifications.size());

        when(memberRepository.findById(1L))
            .thenReturn(Optional.of(testMember));
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
                tuple("comment", 1L)
            );

        verify(memberRepository).findById(1L);
        verify(notificationRepository).findAllByMemberAndReadFalse(testMember, pageable);
    }

    @Test
    void 알림_수신_실패_회원정보없음() {
        // given
        when(memberRepository.findById(1L))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.receiveNotification(customUser, pageable))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findById(1L);
        verify(notificationRepository, never()).findAllByMemberAndReadFalse(any(), any());
    }

    @Test
    void 알림_수신_성공_알림없음() {
        // given
        Page<Notification> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(notificationRepository.findAllByMemberAndReadFalse(testMember, pageable))
            .thenReturn(emptyPage);

        // when
        Page<NotificationResponse> result = notificationService.receiveNotification(customUser, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(memberRepository).findById(1L);
        verify(notificationRepository).findAllByMemberAndReadFalse(testMember, pageable);
    }


    @Test
    void 알림설정_토글_성공() {
        // given
        String type = "comment";
        NotificationSetting notificationSetting = NotificationSetting.builder()
            .member(testMember)
            .type(type)
            .disabled(false)
            .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(notificationSettingRepository.findByMemberAndType(testMember, type))
            .thenReturn(Optional.of(notificationSetting));

        // when
        notificationService.toggleNotification(customUser, type);

        // then
        assertThat(notificationSetting.isDisabled()).isTrue();
        verify(memberRepository).findById(1L);
        verify(notificationSettingRepository).findByMemberAndType(testMember, type);
    }

    @Test
    void 알림설정_토글_실패_회원정보없음() {
        // given
        String type = "comment";

        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.toggleNotification(customUser, type))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findById(1L);
        verify(notificationSettingRepository, never()).findByMemberAndType(any(), any());
    }

    @Test
    void 알림설정_토글_실패_알림설정없음() {
        // given
        String type = "comment";

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(notificationSettingRepository.findByMemberAndType(testMember, type))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.toggleNotification(customUser, type))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findById(1L);
        verify(notificationSettingRepository).findByMemberAndType(testMember, type);
    }

    @Test
    void 알림설정_토글_성공_알림켜기() {
        // given
        String type = "comment";
        NotificationSetting notificationSetting = NotificationSetting.builder()
            .member(testMember)
            .type(type)
            .disabled(true)
            .build();

        when(memberRepository.findById(1L))
            .thenReturn(Optional.of(testMember));
        when(notificationSettingRepository.findByMemberAndType(testMember, type))
            .thenReturn(Optional.of(notificationSetting));

        // when
        notificationService.toggleNotification(customUser, type);

        // then
        assertThat(notificationSetting.isDisabled()).isFalse();
        verify(memberRepository).findById(1L);
        verify(notificationSettingRepository).findByMemberAndType(testMember, type);
    }


}