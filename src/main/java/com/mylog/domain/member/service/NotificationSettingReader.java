package com.mylog.domain.member.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.dto.NotificationSettingResponse;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.repository.NotificationSettingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingReader {

  private final NotificationSettingRepository notificationSettingRepository;

  public boolean isDisabled(Member member, String type) {
    return notificationSettingRepository
        .findByMemberIdAndType(member.getId(), type)
        .orElseThrow(() -> BusinessException.error(ErrorCode.NOTIFICATION_NOT_FOUND))
        .isDisabled();
  }

  public List<NotificationSettingResponse> getNotificationSettings(Long memberId) {
    return notificationSettingRepository.findByMemberId(memberId).stream()
        .map(NotificationSettingResponse::from)
        .toList();
  }
}
