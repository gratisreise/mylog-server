package com.mylog.dto.classes;

import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class CustomUser extends User {
    private final OauthProvider provider;

    public CustomUser(Member member, Collection<? extends GrantedAuthority> authorities) {
        super(member.getEmail(), member.getPassword(), authorities);
        this.provider = member.getProvider();
    }

    public CustomUser(Long id, Collection<? extends GrantedAuthority> authorities) {
        super(id.toString(), "", authorities);
        this.provider = OauthProvider.SOCIAL;
    }

}
