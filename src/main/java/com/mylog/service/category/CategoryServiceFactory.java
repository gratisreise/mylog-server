package com.mylog.service.category;

import com.mylog.enums.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryServiceFactory {
    private final LocalCategoryService localCategoryService;
    private final SocialCategoryService socialCategoryService;

    public CategoryService getCategoryService(OauthProvider provider){
        return provider == OauthProvider.LOCAL ?
            localCategoryService :
            socialCategoryService;
    }
}
