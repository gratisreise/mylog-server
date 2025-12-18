package com.mylog.classes;

import com.mylog.model.entity.Comment;
import com.mylog.domain.entity.Member;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reply {
    private Long  id;
    private String content;
    private String author;
    private Long memberId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Reply(Comment comment) {
        Member member = comment.getMember();
        this.id = comment.getId();
        this.content = comment.getContent();
        this.author = member.getNickname();
        this.memberId = member.getId();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}
