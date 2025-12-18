package com.mylog.service.notificationsetting;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.notification.NotificationSettingResponse;
import com.mylog.domain.entity.Member;
import com.mylog.model.entity.NotificationSetting;
import com.mylog.repository.notificationsetting.NotificationSettingRepository;
import com.mylog.api.member.MemberReader;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
public class NotificationSettingReaderTest {

    @InjectMocks
    NotificationSettingReader notificationSettingReader;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private MemberReader memberReader;

    private Member member1;
    private NotificationSetting notificationSetting1;
    private NotificationSetting notificationSetting2;
    private CustomUser customUser1;

    private static final String TYPE = "COMMENT";
    @BeforeEach
    void setUP(){
        member1 = Member.builder()
            .id(1L)
            .password("testpassword")
            .email("test@email.com")
            .memberName("testUser")
            .nickname("testNickname")
            .build();

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        customUser1 = new CustomUser(member1, Collections.singletonList(authority));

        notificationSetting1 = NotificationSetting.builder()
            .id(1L)
            .type(TYPE)
            .disabled(true)
            .member(member1)
            .build();

        notificationSetting2 = NotificationSetting.builder()
            .id(1L)
            .type("ARTICLE")
            .disabled(true)
            .member(member1)
            .build();
    }


    @Nested
    class 알림설정켜짐확인{ // 알림설정켜짐확인
        /**
         * 정상반환
         * 예외반환
         */
        @Test
        void isDisabled_성공_불리언반환(){
            //given
            when(notificationSettingRepository.findByMemberAndType(member1, TYPE))
                .thenReturn(Optional.of(notificationSetting1));

            //when
            boolean disabled = notificationSettingReader.isDisabled(member1, TYPE);

            //then
            verify(notificationSettingRepository).findByMemberAndType(member1, TYPE);
            assertThat(disabled).isTrue();
        }

        @Test
        void isDisabled_설정없음_예외발생(){
            //given
            when(notificationSettingRepository.findByMemberAndType(member1, TYPE))
                .thenReturn(Optional.empty());

            //when
            assertThatThrownBy(()->notificationSettingReader.isDisabled(member1, TYPE))
                .isInstanceOf(CMissingDataException.class);

            //then
            verify(notificationSettingRepository).findByMemberAndType(member1, TYPE);
        }

    }

    @Nested
    class 알림설정목록조회{
        /** getNotificationSettings()
         * 정상2개반환
         * 정상0개반환
         * 유저조회실패예외반환
         *
         */
        @Test
        void 알림설정목록조회_성공_2개(){
            //given
            List<NotificationSetting> settings = List.of(notificationSetting1, notificationSetting2);

            when(memberReader.getById(customUser1.getMemberId())).thenReturn(member1);
            when(notificationSettingRepository.findByMember(member1))
                .thenReturn(settings);

            //when
            List<NotificationSettingResponse> result = notificationSettingReader
                .getNotificationSettings(customUser1);

            //then
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.get(0).type()).isEqualTo(TYPE);
            assertThat(result.get(0).disabled()).isEqualTo(true);
            assertThat(result.get(1).type()).isEqualTo("ARTICLE");
            verify(memberReader).getById(eq(1L));
            verify(notificationSettingRepository).findByMember(member1);
        }

        @Test
        void 알림설정목록조회_성공_0개(){
            //given
            when(memberReader.getById(customUser1.getMemberId())).thenReturn(member1);
            when(notificationSettingRepository.findByMember(member1))
                .thenReturn(List.of());

            //when
            List<NotificationSettingResponse> result = notificationSettingReader
                .getNotificationSettings(customUser1);

            //then
            assertThat(result.size()).isEqualTo(0);
            assertThatThrownBy(() -> result.get(0))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);
            verify(memberReader).getById(eq(1L));
            verify(notificationSettingRepository).findByMember(member1);
        }

        @Test
        void 알림설정목록조회_유저없음_예외발생(){
            //given
            when(memberReader.getById(customUser1.getMemberId()))
                .thenThrow(CMissingDataException.class);

            //when
            assertThatThrownBy(() -> notificationSettingReader.getNotificationSettings(customUser1))
                .isInstanceOf(CMissingDataException.class);

            //then
            verify(memberReader).getById(eq(1L));
            verify(notificationSettingRepository, never()).findByMember(any(Member.class));
        }


    }
}
