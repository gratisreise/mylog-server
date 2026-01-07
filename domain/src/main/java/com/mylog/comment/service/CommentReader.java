package com.mylog.comment.service;

import com.mylog.article.service.ArticleReader;
import com.mylog.comment.entity.Comment;
import com.mylog.comment.repository.CommentRepository;
import com.mylog.exception.CMissingDataException;
import com.mylog.member.service.MemberReader;
import java.nio.channels.FileChannel;
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
    private final ArticleReader articleReader;

//    //내가 작성한 댓글
//    public Page<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable) {
//        Member member = memberReader.getById(customUser.getMemberId());
//        return commentRepository.findAllByMember(member, pageable)
//            .map(CommentResponse::from);
//    }
//
//    //내 게시글의 댓글
//    public Page<CommentResponse> getComments(CustomUser customUser, Pageable pageable) {
//        Member member = memberReader.getById(customUser.getMemberId());
//        return commentRepository.findMyArticlesComments(member, pageable)
//            .map(CommentResponse::from);
//    }
//
    //게시글 상세 댓글 목록조회
    public Page<Comment> getComments(Long articleId, Pageable pageable){
        if(!articleReader.isExists(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글 입니다.");
        }

        return commentRepository.findByArticle_IdAndParentId(articleId, 0L, pageable);
    }

    //대댓글 목록조회
    public Page<Comment> getComments(Long articleId, Long parentId, Pageable pageable){
        if(!articleReader.isExists(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글 입니다.");
        }

        return commentRepository.findByArticle_IdAndParentId(articleId, parentId, pageable);
    }

//
//    public Comment getById(Long commentId) {
//        return commentRepository.findById(commentId).orElseThrow(CMissingDataException::new);
//    }
//
//    private CommentArticleResponse getCommentArticleResponse(Comment comment){
//        List<Reply> replies = getReplies(comment);
//        return CommentArticleResponse.of(comment, replies);
//    }
//
//
//    private List<Reply> getReplies(Comment comment){
//        long articleId = comment.getArticle().getId();
//        long parentId = comment.getId();
//        List<Comment> comments = commentRepository.findByArticle_IdAndParentId(articleId, parentId);
//        return comments.stream().map(Reply::new).toList();
//    }

}
