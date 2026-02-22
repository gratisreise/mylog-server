<<<<<<<< HEAD:src/main/java/com/mylog/domain/notification/Notification.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/notification/Notification.java
package com.mylog.domain.notification;

import com.mylog.domain.member.Member;
========
package com.mylog.notification.entity;

import com.mylog.member.entity.Member;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/notification/entity/Notification.java
========
package com.mylog.notification.entity;

import com.mylog.member.entity.Member;
>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:domain/src/main/java/com/mylog/notification/entity/Notification.java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
@AllArgsConstructor
@Getter
@Setter
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Long relatedId;

    @Column(nullable = false)
    private boolean read;

    @CreatedDate
    private LocalDateTime createdAt;

    public Notification(Member member, Long relatedId, String type) {
        this.member = member;
        this.type = type;
        this.relatedId = relatedId;
    }

    public void read() {
        this.read = true;
    }
}
