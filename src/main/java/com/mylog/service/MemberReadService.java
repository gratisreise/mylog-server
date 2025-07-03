package com.mylog.service;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberReadService {
    private final MemberRepository memberRepository;

    //Request도입 => password 노출 중 형태 변경
    public Member getMember(CustomUser customUser){
        return memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
    }


    public Member getById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(CMissingDataException::new);
    }

    public Member getByNickname(String author) {
        return memberRepository.findByNickname(author)
            .orElseThrow(CMissingDataException::new);
    }
}
