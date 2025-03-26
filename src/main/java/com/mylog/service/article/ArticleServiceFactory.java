package com.mylog.service.article;

import com.mylog.enums.OauthProvider;
import com.mylog.service.member.LocalMemberService;
import com.mylog.service.member.MemberService;
import com.mylog.service.member.SocialMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleServiceFactory {
    private final LocalArticleService localArticleService;
    private final SocialArticleService socialArticleService;

    public ArticleService getMemberService(OauthProvider provider){
        return provider == OauthProvider.LOCAL ?
            localArticleService :
            socialArticleService;
    }
}
