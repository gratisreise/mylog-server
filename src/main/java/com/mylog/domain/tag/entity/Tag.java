<<<<<<<< HEAD:src/main/java/com/mylog/domain/tag/entity/Tag.java
package com.mylog.domain.tag.entity;
========
package com.mylog.tag.entity;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/tag/entity/Tag.java

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, unique = true)
    private String tagName;

    @CreatedDate
    private LocalDate createdAt;

    public Tag(String tagName) {
        this.tagName = tagName;
    }
}
