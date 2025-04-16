package com.mylog.service.member;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.SignUpRequest;
import com.mylog.dto.UpdateMemberRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import com.mylog.service.S3Service;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@ServiceType(OauthProvider.SOCIAL)
@Transactional(readOnly = true)
public class SocialMemberService implements MemberService{
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public void saveMember(SignUpRequest request) {
        throw new CInvalidDataException("소셜 유저는 저장 메서드 별도로 존재");
    }

    @Override
    public Member getMember(CustomUser customUser) {
        return memberRepository
            .findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);
    }

    @Override
    @Transactional
    public void updateMember(
        UpdateMemberRequest request,
        CustomUser customUser,
        MultipartFile file
    ) throws IOException {

        Member member = memberRepository
            .findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);

        member.update(request);

        String imageUrl = getImageUrl(file, member);

        member.setProfileImg(imageUrl);
    }

    private String getImageUrl(MultipartFile file, Member member) throws IOException {
        s3Service.deleteImage(member.getProfileImg());
        return s3Service.upload(file)
            .orElseThrow(CMissingDataException::new);
    }

    @Override
    @Transactional
    public void deleteMember(CustomUser customUser) {
        memberRepository.deleteById(Long.valueOf(customUser.getUsername()));
    }


}
