<<<<<<<< HEAD:src/main/java/com/mylog/common/db/BaseEntity.java
<<<<<<<< HEAD:src/main/java/com/mylog/common/db/BaseEntity.java
package com.mylog.common.db;
========
package com.mylog;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/BaseEntity.java
========
package com.mylog;
>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:domain/src/main/java/com/mylog/BaseEntity.java

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
