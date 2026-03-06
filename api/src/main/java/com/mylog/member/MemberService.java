package com.mylog.member;


import com.mylog.auth.classes.CustomUser;
import com.mylog.auth.dto.SignUpRequest;
import com.mylog.category.service.CategoryWriter;
import com.mylog.member.dto.MemberResponse;
import com.mylog.member.dto.MemberUpdateRequest;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import com.mylog.member.service.MemberWriter;
import com.mylog.notification.service.NotificationSettingWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberReader memberReader;
    private final MemberWriter memberWriter;
    private final CategoryWriter categoryWriter;
    private final NotificationSettingWriter notificationSettingWriter;
    private final PasswordEncoder encoder;

    public MemberResponse getMember(CustomUser customUser) {
        Member member = memberReader.getById(customUser.getMemberId());
        return MemberResponse.from(member);
    }

    @Transactional
    public void updateMember(MemberUpdateRequest request, String imageUrl, CustomUser customUser) {
        Member member = request.toEntity(encoder, imageUrl);
        Long memberId = customUser.getMemberId();
        memberWriter.updateMember(member, memberId);
    }

    @Transactional
    public void deleteMember(CustomUser customUser) {
        Long memberId = customUser.getMemberId();
        memberWriter.deleteMember(memberId);
        memberReader.isDeleted(memberId);
    }


    /** 회원가입
     * 알림 엔티티 생성
     * 비동기로 카테고리 생성
     * @param request
     */
    @Transactional
    public void saveMember(SignUpRequest request) {
        //중복검증
        memberReader.isDuplicated(request.email());

        //회원생성
        Member member = request.toEntity(encoder);
        Member savedMember = memberWriter.saveMember(member);

        //카테고리 생성(비동기)
        categoryWriter.createCategory(savedMember);

        //알림설정생성
        notificationSettingWriter.createNotificationSetting(savedMember);
    }
}
