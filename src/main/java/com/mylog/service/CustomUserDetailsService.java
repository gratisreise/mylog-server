package com.mylog.service;

import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByNickname(username)
            .orElseThrow(CMissingDataException::new);

        return createUserDetails(member);
    }


    private UserDetails createUserDetails(Member member) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return new CustomUser(member, Collections.singleton(authority));
    }
}