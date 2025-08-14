package com.mylog.repository.comment;

import com.mylog.model.entity.Comment;
import com.mylog.model.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom  {


    Page<Comment> findMyArticlesComments(Member member, Pageable pageable);

}
