package com.mylog.service.comment;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.comment.CommentCreateRequest;
import com.mylog.dto.comment.CommentResponse;
import com.mylog.entity.Article;
import com.mylog.entity.Comment;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CommentRepository;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@ServiceType(OauthProvider.SOCIAL)
@RequiredArgsConstructor
public class SocialCommentService implements CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;

    @Override
    public void createComment(CommentCreateRequest request, CustomUser customUser) {
        //게시글 아이디로 게시글 작성자 찾기
        Article article = articleRepository.findById(request.getArticleId())
            .orElseThrow(CMissingDataException::new);

        //유저객체로 댓글 작성자 찾기
        Member member = memberRepository
            .findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);

        //커맨트 객체 생성
        Comment comment =  Comment.builder()
            .article(article)
            .member(member)
            .content(request.getContent())
            .parentId(request.getParentCommentId())
            .build();

        //저장
        commentRepository.save(comment);
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
