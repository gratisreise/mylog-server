package com.mylog.service.comment;

import com.mylog.classes.Reply;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.comment.CommentArticleResponse;
import com.mylog.model.dto.comment.CommentResponse;
import com.mylog.model.entity.Comment;
import com.mylog.domain.entity.Member;
import com.mylog.api.article.ArticleRepository;
import com.mylog.repository.comment.CommentRepository;
import com.mylog.api.member.MemberReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentReader {

    private final CommentRepository commentRepository;
    private final MemberReader memberReader;
    private final ArticleRepository articleRepository;

    //내가 작성한 댓글
    public Page<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable) {
        Member member = memberReader.getById(customUser.getMemberId());
        return commentRepository.findAllByMember(member, pageable)
            .map(CommentResponse::new);
    }

    //내 게시글의 댓글
    public Page<CommentResponse> getComments(CustomUser customUser, Pageable pageable) {
        Member member = memberReader.getById(customUser.getMemberId());
        return commentRepository.findMyArticlesComments(member, pageable)
            .map(CommentResponse::new);
    }

    //게시글 상세 댓글 목록조회
    public Page<CommentArticleResponse> getComments(Long articleId, Pageable pageable){
        if(!articleRepository.existsById(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글 입니다.");
        }
        return commentRepository.findByArticle_IdAndParentId(articleId, 0L, pageable)
            .map(comment -> new CommentArticleResponse(comment, getReplies(comment)));
    }


    private List<Reply> getReplies(Comment comment){
        long articleId = comment.getArticle().getId();
        long parentId = comment.getId();
        List<Comment> comments = commentRepository.findByArticle_IdAndParentId(articleId, parentId);
        return comments.stream().map(Reply::new).toList();
    }

    public Comment getById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(CMissingDataException::new);
    }
}
