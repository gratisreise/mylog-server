package com.mylog.api.comment;

import com.mylog.domain.entity.Comment;
import com.mylog.domain.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom  {


    Page<Comment> findMyArticlesComments(Member member, Pageable pageable);

}
