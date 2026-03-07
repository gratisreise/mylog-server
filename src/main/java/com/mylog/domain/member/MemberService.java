package com.mylog.domain.member;

import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.member.service.MemberWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberReader memberReader;
    private final MemberWriter memberWriter;
}
