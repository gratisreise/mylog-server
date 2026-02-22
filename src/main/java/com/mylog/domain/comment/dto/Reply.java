<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/dto/Reply.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/dto/Reply.java
package com.mylog.domain.comment.dto;

import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.member.Member;
========
package com.mylog.comment.classes;

import com.mylog.comment.entity.Comment;
import com.mylog.member.entity.Member;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/comment/classes/Reply.java
========
package com.mylog.comment.classes;

import com.mylog.comment.entity.Comment;
import com.mylog.member.entity.Member;
>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:domain/src/main/java/com/mylog/comment/classes/Reply.java
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
