package com.mylog.domain.member.entity;

import com.mylog.common.db.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(
    name = "custom_writing_style",
    indexes = {
      @Index(name = "idx_custom_style_member", columnList = "member_id"),
      @Index(name = "idx_custom_style_member_name", columnList = "member_id, name", unique = true)
    })
public class CustomWritingStyle extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(length = 30, nullable = false)
  private String name;

  @Column(length = 50, nullable = false)
  private String role;

  @Column(length = 200, nullable = false)
  private String instruction;

  public static CustomWritingStyle create(
      Member member, String name, String role, String instruction) {
    return CustomWritingStyle.builder()
        .member(member)
        .name(name)
        .role(role)
        .instruction(instruction)
        .build();
  }

  public void update(String name, String role, String instruction) {
    if (name != null) {
      this.name = name;
    }
    if (role != null) {
      this.role = role;
    }
    if (instruction != null) {
      this.instruction = instruction;
    }
  }

  public String generatePrompt(String originalContent) {
    return String.format(
        """
        # Role
        너는 %s야.

        # Task
        입력된 블로그 게시글을 지정된 문체로 변환해줘.

        # Constraint
        - 원문의 핵심 내용과 의미는 유지할 것.
        - 지시사항에 맞는 톤과 스타일로 자연스럽게 재작성할 것.
        - 출력은 변환된 텍스트만 반환할 것 (추가 설명 없이).

        # Instruction
        %s

        # Input Content
        %s
        """,
        this.role, this.instruction, originalContent);
  }
}
