package com.mylog.service;

import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Member member = memberRepository.findById(Long.valueOf(id)).orElseThrow();
        UserDetails userDetails;

        return member.getProvider() == OauthProvider.LOCAL ?
            loadUserByEmail(member.getEmail()) :
            loadUserById(member.getId());
    }

    private UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        if(!memberRepository.existsById(id))
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        return createUserDetails(id);
    }

    private UserDetails loadUserByEmail(String email) {
        if(!memberRepository.existsByEmail(email))
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        return createUserDetails(memberRepository.findByEmail(email).orElseThrow());
    }

    private UserDetails createUserDetails(Long id) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return new CustomUser(id, Collections.singleton(authority));
    }

    private UserDetails createUserDetails(Member member) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return new CustomUser(member, Collections.singleton(authority));
    }
}