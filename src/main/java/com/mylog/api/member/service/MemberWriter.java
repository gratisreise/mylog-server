package com.mylog.api.member.service;

import com.mylog.api.auth.dto.SignUpRequest;
import com.mylog.api.member.dto.UpdateMemberRequest;
import com.mylog.api.member.entity.Member;
import com.mylog.api.member.repository.MemberRepository;
import com.mylog.common.exception.CDuplicatedException;
import com.mylog.common.exception.CUnAuthorizedException;
import com.mylog.api.auth.CustomUser;
import com.mylog.infra.s3.S3Service;
import com.mylog.api.category.service.CategoryWriter;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MemberWriter {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final CategoryWriter categoryWriter;
    private final MemberReader memberReader;

    @Value("${cloud.aws.s3.basic}")
    private String basicImageUrl;

    public void saveMember(SignUpRequest request){
        if(isExists(request.email())){
            throw new CDuplicatedException("이미 존재하는 이메일입니다.");
        }

        Member member = request.toEntity(passwordEncoder, basicImageUrl);

        Member savedMember = memberRepository.save(member);

        //멤버 생성 => 카테고리 자동생성
        categoryWriter.createCategory(member);
    }

    public void updateMember(UpdateMemberRequest request,
        CustomUser customUser, MultipartFile file) throws IOException{

        Member member = memberReader.getById(customUser.getMemberId());

        if(validateNickname(member.getNickname(), request.nickname())){
            throw new CDuplicatedException("중복되는 닉네임 입니다.");
        }

        if(file == null){ //기존이랑 사진 동일
            member.update(request, passwordEncoder);
            return;
        }

        //기존이랑 이미지 다름
        imageExistedUpdate(request, file, member);
    }


    public void deleteMember(CustomUser customUser){
        Member member = memberReader.getById(customUser.getMemberId());
        Long customUserId = customUser.getMemberId();

        if(!member.isOwnedBy(customUserId)){
            throw new CUnAuthorizedException("멤버 삭제에 대한 권한인 없습니다.");
        }

        deleteImage(member.getProfileImg());

        memberRepository.deleteById(customUserId);
    }


    private void imageExistedUpdate(UpdateMemberRequest request, MultipartFile file, Member member)
        throws IOException {
        String profileImg = file.getOriginalFilename();
        String memberImg = member.getProfileImg();
        String originMemberImg = memberImg.substring(93);

        if(profileImg.equals(originMemberImg)){
            member.update(request, passwordEncoder);
        } else {
            profileImg = s3Service.upload(file);
            deleteImage(memberImg);
            member.update(request, profileImg);
        }
    }

    private boolean isExists(String email){
        return memberRepository.existsByEmail(email);
    }
    private boolean validateNickname(String origin, String request){
        return origin.equals(request) || memberRepository.existsByNickname(request);
    }

    private void deleteImage(String memberImg) {
        if(!memberImg.equals(basicImageUrl)) s3Service.deleteImage(memberImg);
    }
}
