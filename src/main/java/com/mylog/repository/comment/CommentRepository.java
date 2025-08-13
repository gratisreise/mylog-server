package com.mylog.repository.comment;

import com.mylog.model.entity.Comment;
import com.mylog.model.entity.Member;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom{

    //게시글 댓글 목록 조회
    @EntityGraph(attributePaths = {"article", "member"})
    Page<Comment> findByArticle_Id(Long articleId, Pageable pageable);

    //대댓글 목록 조회
    @EntityGraph(attributePaths = {"article", "member"})
    Page<Comment> findByArticle_IdAndParentId(Long articleId, Long parentId, Pageable pageable);

    //나의 댓글 조회

    Page<Comment> findAllByMember(Member member, Pageable pageable);

    //나의 게시글 댓글 조회
    @EntityGraph(attributePaths = {"article", "member"})
    Page<Comment> findAllByArticle_Member(Member member, Pageable pageable);

    //답글조회
    @EntityGraph(attributePaths = {"article", "member"})
    List<Comment> findByArticle_IdAndParentId(Long articleId, Long parentId);

}
