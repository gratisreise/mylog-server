package com.mylog.comment.service;


import com.mylog.comment.entity.Comment;
import com.mylog.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentWriter {

    private final CommentRepository commentRepository;

    public void create(Comment comment) {
        commentRepository.save(comment);
    }

    public void deleteById(Long commentId){
        commentRepository.deleteById(commentId);
    }

}
