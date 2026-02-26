package com.mylog.domain.category;

import com.mylog.common.CommonValue;
import com.mylog.common.db.BaseEntity;
import com.mylog.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 12)
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;



    public boolean isOwnedBy(Long memberId) {
        return Objects.equals(memberId, member.getId());
    }

    // 정적 팩토리 메서드: 비즈니스 의미를 담은 이름을 부여
    public static Category createDefault(Member member) {
        return Category.builder()
            .member(member)
            .categoryName(CommonValue.BASIC_CATEGORY)
            .build();
    }

    public void updateCategorName(String categoryName) {
        this.categoryName = categoryName;
    }
}
