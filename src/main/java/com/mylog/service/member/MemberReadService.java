package com.mylog.service.member;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.member.MemberResponse;
import com.mylog.model.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberReadService {
    private final MemberRepository memberRepository;

    public MemberResponse getMember(CustomUser customUser){
        Member member = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
        return new MemberResponse(member);
    }

    public Member getById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(CMissingDataException::new);
    }

    public Member getByNickname(String author) {
        return memberRepository.findByNickname(author).orElseThrow(CMissingDataException::new);
    }

    public Member getByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(CMissingDataException::new);
    }

    public Member getByCustomUser(CustomUser customUser) {
        return memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
    }
}
