package com.mylog.service.member;

import com.mylog.exception.CDuplicatedException;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.member.SignUpRequest;
import com.mylog.model.dto.member.UpdateMemberRequest;
import com.mylog.model.entity.Member;
import com.mylog.repository.member.MemberRepository;
import com.mylog.service.S3Service;
import com.mylog.service.category.CategoryService;
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
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final CategoryService categoryService;
    private final MemberReader memberReader;

    @Value("${cloud.aws.s3.basic}")
    private String basicImageUrl;

    public void saveMember(SignUpRequest request){
        if(memberRepository.existsByEmail(request.email())){
            throw new CDuplicatedException("이미 존재하는 이메일입니다.");
        }

        String cryptedPassword = passwordEncoder.encode(request.password());
        log.info("cryptedPassword: {}", cryptedPassword);

        if (cryptedPassword == null) {
            throw new CMissingDataException("비밀번호 암호화를 실패했습니다.");
        }

        Member member = new Member(request, cryptedPassword, basicImageUrl);

//        log.info("member: {}", member.toString());
        memberRepository.save(member);

        //비동기 처리
        categoryService.createCategory(request.email());
    }

    public void updateMember(UpdateMemberRequest request, CustomUser customUser, MultipartFile file)
        throws IOException{

        Member member = memberReader.getById(customUser.getMemberId());

        if(validateNickname(member.getNickname(), request.nickname())){
            throw new CDuplicatedException("중복되는 닉네임 입니다.");
        }

        if(request.password() != null){ //패스워드 암호화
            request = createUpdateRequest(request);
        }

        if(file == null){ //기존이랑 사진 동일
            member.update(request);
            return;
        }

        String profileImg = file.getOriginalFilename();
        String memberImg = member.getProfileImg();
        String originMemberImg = memberImg.substring(93);

        if(profileImg.equals(originMemberImg)){
            member.update(request);
        } else {
            profileImg = s3Service.upload(file);
            deleteImage(memberImg);
            member.update(request, profileImg);
        }
    }

    public void deleteMember(CustomUser customUser){
        Member member = memberReader.getById(customUser.getMemberId());
        String memberImage = member.getProfileImg();

        deleteImage(memberImage);

        memberRepository.deleteById(customUser.getMemberId());
    }

    private boolean validateNickname(String origin, String request){
        return !origin.equals(request) && memberRepository.existsByNickname(request);
    }

    private void deleteImage(String memberImg) {
        if(!memberImg.equals(basicImageUrl)) s3Service.deleteImage(memberImg);
    }

    private UpdateMemberRequest createUpdateRequest(UpdateMemberRequest request) {
        return new UpdateMemberRequest(
            passwordEncoder.encode(request.password()),
            request.memberName(),
            request.nickname(),
            request.bio(),
            request.imageUrl()
        );
    }
}
