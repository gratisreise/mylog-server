package com.mylog.repository;

import com.mylog.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    //게시글 댓글 목록 조회
    Page<Comment> findByArticleId(Long articleId, Pageable pageable);

    //대댓글 목록 조회

    //나의 댓글 조회

    //나의 게시글 댓글 조회

}
