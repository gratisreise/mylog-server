package com.mylog.api.comment.repository;

import com.mylog.api.comment.entity.Comment;
import com.mylog.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom  {


    Page<Comment> findMyArticlesComments(Member member, Pageable pageable);

}
