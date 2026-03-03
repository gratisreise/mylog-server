package com.mylog.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WritingStyle {
    FRIENDLY("친근한 블로거", "이모지를 적절히 사용하고, '~해요' 체로 친절하게 작성해줘."),
    PROFESSIONAL("비즈니스 전문가", "격식 있는 단어를 선택하고, 논리적인 '~입니다' 체로 작성해줘."),
    SHORT_FORM("MZ세대 SNS 담당자", "최신 유행어를 섞어서 아주 짧고 강렬하게 핵심만 전달해줘.");

    private final String role;
    private final String instruction;

    public String generatePrompt(String originalContent) {
        return String.format(
            "너는 %s야. 아래 내용을 지시사항에 맞춰서 변환해줘.\n\n" +
            "지시사항: %s\n" +
            "내용: %s",
            this.role, this.instruction, originalContent
        );
    }
}