package com.mylog.domain.member.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.entity.CustomWritingStyle;
import com.mylog.domain.member.repository.CustomWritingStyleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomWritingStyleReader {

  private final CustomWritingStyleRepository customWritingStyleRepository;

  public List<CustomWritingStyle> getCustomStyles(Long memberId) {
    return customWritingStyleRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
  }

  public CustomWritingStyle getByIdAndMemberId(Long styleId, Long memberId) {
    return customWritingStyleRepository
        .findByIdAndMemberId(styleId, memberId)
        .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOM_STYLE_NOT_FOUND));
  }
}
