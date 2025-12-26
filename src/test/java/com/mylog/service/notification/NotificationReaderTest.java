package com.mylog.service.notification;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.api.notification.service.NotificationReader;
import com.mylog.exception.CMissingDataException;
import com.mylog.api.auth.CustomUser;
import com.mylog.api.notification.dto.NotificationResponse;
import com.mylog.api.member.entity.Member;
import com.mylog.api.notification.entity.Notification;
import com.mylog.api.notification.repository.NotificationRepository;
import com.mylog.api.member.service.MemberReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

@ExtendWith(MockitoExtension.class)
public class NotificationReaderTest {
    @InjectMocks
    NotificationReader notificationReader;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MemberReader memberReader;

    private Notification notification1;
    private Member member1;
    private CustomUser customUser1;

    @BeforeEach
    void setUP(){
        notification1 = Notification.builder()
            .id(1L)
            .type("comment")
            .read(false)
            .relatedId(1L)
            .build();

        member1 = Member.builder()
            .id(1L)
            .email("test@email.com")
            .password("password")
            .memberName("test1")
            .build();

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        customUser1 = new CustomUser(member1, Collections.singletonList(authority));
    }




    @Nested
    class getByIdTests{

        @Test
        void 알림목록조회_정상반환(){
            //given
            Long commentId = 1L;
            when(notificationRepository.findById(commentId)).thenReturn(Optional.of(notification1));

            //when
            Notification notification = notificationReader.getById(commentId);

            //then
            verify(notificationRepository).findById(commentId);
            assertThat(notification.getId()).isEqualTo(commentId);
        }

        @Test
        void 알림목록조회_알림없음_예외반환(){
            //given
            Long notificationId = 1L;
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

            //when
            assertThatThrownBy(() -> notificationReader.getById(notificationId))
                .isInstanceOf(CMissingDataException.class);

            //then
            verify(notificationRepository).findById(notificationId);
        }


    }


    @Nested
    class receiveNotificationTests {
        @Test
        void 알림목록조회_정상반환(){
            //given
            Pageable pageable = PageRequest.of(0, 10);
            List<Notification> notifications = List.of(notification1);
            Page<Notification> notificationPage = new PageImpl<>(notifications, pageable, notifications.size());
            when(memberReader.getByCustomUser(customUser1)).thenReturn(member1);
            when(notificationRepository.findByMemberAndRead(member1, pageable))
                .thenReturn(notificationPage);
            //when
            Page<NotificationResponse> result  =
                notificationReader.receiveNotification(customUser1, pageable);

            //then
            verify(memberReader).getByCustomUser(customUser1);
            verify(notificationRepository).findByMemberAndRead(member1, pageable);
            assertThat(result.getContent().size()).isEqualTo(notifications.size());
            assertThat(result.getContent().get(0).notificationId())
                .isEqualTo(notifications.get(0).getId());
        }

        @Test
        void 알림목록조회_유저없음_예외반환(){
            //given
            Pageable pageable = PageRequest.of(0, 10);

            when(memberReader.getByCustomUser(customUser1)).thenThrow(CMissingDataException.class);

            //when
            assertThatThrownBy(()->notificationReader.receiveNotification(customUser1, pageable))
                .isInstanceOf(CMissingDataException.class);

            //then
            verify(memberReader).getByCustomUser(customUser1);
            verify(notificationRepository, never()).findByMemberAndRead(member1, pageable);
        }

    }
}
