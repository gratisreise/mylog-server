package com.mylog.member.service;


import com.mylog.member.entity.Member;
import com.mylog.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mylog.exception.CMissingDataException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberReader {
    private final MemberRepository memberRepository;

//    public MemberResponse getMember(CustomUser customUser){
//        Member member = memberRepository.findById(customUser.getMemberId())
//            .orElseThrow(CMissingDataException::new);
//        return new MemberResponse(member);
//    }
//
    public Member getById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(CMissingDataException::new);
    }

//
//    public Member getByNickname(String author) {
//        return memberRepository.findByNickname(author).orElseThrow(CMissingDataException::new);
//    }
//
//    public Member getByEmail(String email) {
//        return memberRepository.findByEmail(email).orElseThrow(CMissingDataException::new);
//    }
//
//    public Member getByCustomUser(CustomUser customUser) {
//        return memberRepository.findById(customUser.getMemberId())
//            .orElseThrow(CMissingDataException::new);
//    }
}
