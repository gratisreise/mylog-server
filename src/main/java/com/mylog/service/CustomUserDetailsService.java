package com.mylog.service;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Member;
import com.mylog.service.member.MemberReader;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberReader memberReader;

    //회원가입할 때는 이메일로 찾고 모든 토큰 요청마다 검증은 id로 하고
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = username.contains("@") ?
            memberReader.getByEmail(username) :
            memberReader.getById(Long.parseLong(username));
        return createUserDetails(member);
    }

    private UserDetails createUserDetails(Member member) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return new CustomUser(member, Collections.singleton(authority));
    }
}
