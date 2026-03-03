package com.mylog.domain.comment.repository;

import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom  {


    Page<Comment> findMyArticlesComments(Member member, Pageable pageable);

}
