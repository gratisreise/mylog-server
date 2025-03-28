package com.mylog.service.comment;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.comment.CommentCreateRequest;
import com.mylog.dto.comment.CommentResponse;
import com.mylog.enums.OauthProvider;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CommentRepository;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@ServiceType(OauthProvider.LOCAL)
@RequiredArgsConstructor
public class LocalCommentService implements CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;

    @Override
    public void createComment(CommentCreateRequest request, CustomUser customUser) {

    }

    @Override
    public void updateComment(Long commentId, CustomUser customUser) {

    }

    @Override
    public void deleteComment(Long commentId, CustomUser customUser) {

    }

    @Override
    public Page<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable) {
        return null;
    }

    @Override
    public Page<CommentResponse> getComments(CustomUser customUser, Pageable pageable) {
        return null;
    }
}
