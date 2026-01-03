package com.mylog.auth;

import com.mylog.enums.OauthProvider;
import com.mylog.member.entity.Member;

import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class CustomUser extends User {

    private final Long memberId;
    private final OauthProvider provider;


    public CustomUser(Member member, Collection<? extends GrantedAuthority> authorities) {
        super(String.valueOf(member.getId()), member.getPassword(), authorities);
        this.memberId = member.getId();
        this.provider = member.getProvider();
    }
}
