package com.mylog.domain.comment.repository;

import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.member.entity.Member;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  // 게시글의 부모 댓글 목록 조회 (parent가 null인 댓글)
  @EntityGraph(attributePaths = {"member"})
  Page<Comment> findByArticle_IdAndParentIsNull(Long articleId, Pageable pageable);

  // 나의 댓글 조회
  @Query("SELECT c FROM Comment c JOIN FETCH c.member")
  Page<Comment> findAllByMember(Member member, Pageable pageable);

  // 대댓글 목록 조회 (특정 부모 댓글의 자식들)
  @EntityGraph(attributePaths = {"member"})
  List<Comment> findByParent_Id(Long parentId);

  // 대댓글 페이징 조회
  @EntityGraph(attributePaths = {"member"})
  Page<Comment> findByParent_Id(Long parentId, Pageable pageable);

  // 내가 작성한 댓글 조회
  Page<Comment> findByMemberId(Long memberId, Pageable pageable);
}
