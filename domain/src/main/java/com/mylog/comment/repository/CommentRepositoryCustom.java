package com.mylog.comment.repository;

import com.mylog.comment.entity.Comment;
import com.mylog.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom  {

    Page<Comment> findMyArticlesCommentsByMemberId(Long memberId, Pageable pageable);
}
