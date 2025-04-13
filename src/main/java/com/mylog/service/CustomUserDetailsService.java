package com.mylog.service;

import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
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
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Member member = memberRepository.findById(Long.valueOf(id))
            .orElseThrow(CMissingDataException::new);

        return member.getProvider() == OauthProvider.LOCAL ?
            createLocalUserDetails(member) :
            createSocialUserDetails(id);
    }

    private UserDetails createSocialUserDetails(String id) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return new CustomUser(id, Collections.singleton(authority));
    }

    private UserDetails createLocalUserDetails(Member member) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return new CustomUser(member, Collections.singleton(authority));
    }
}