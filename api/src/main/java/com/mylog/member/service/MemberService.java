package com.mylog.member.service;


import com.mylog.auth.CustomUser;
import com.mylog.member.dto.MemberResponse;
import com.mylog.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberReader memberReader;
    private final MemberWriter memberWriter;


    public MemberResponse getMember(CustomUser customUser) {
        Member member = memberReader.getById(customUser.getMemberId());
        return MemberResponse.from(member);
    }



}
