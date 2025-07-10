package com.mylog.service;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberReadService memberReadService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberReadService.getByNickname(username);
        return createUserDetails(member);
    }


    private UserDetails createUserDetails(Member member) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return new CustomUser(member, Collections.singleton(authority));
    }
}
