package com.mylog.service.comment;


import com.mylog.enums.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentServiceFactory {

    private final LocalCommentService localCommentService;
    private final SocialCommentService socialCommentService;

    public CommentService getCommentService(OauthProvider provider) {
        return provider == OauthProvider.LOCAL ?
            localCommentService :
            socialCommentService;
    }
}
