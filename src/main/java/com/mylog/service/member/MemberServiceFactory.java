package com.mylog.service.member;

import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.service.social.GoogleOAuth2UserService;
import com.mylog.service.social.KakaoOAuth2UserService;
import com.mylog.service.social.NaverOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberServiceFactory {
    private final LocalMemberService localMemberService;
    private final SocialMemberService socialMemberService;

   public MemberService getMemberService(OauthProvider provider){
       return provider == OauthProvider.LOCAL ? localMemberService : socialMemberService;
   }

}