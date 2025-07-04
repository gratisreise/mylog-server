package com.mylog.service;

import com.mylog.model.dto.member.SignUpRequest;
import com.mylog.model.dto.member.UpdateMemberRequest;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberWriteService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final CategoryWriteService categoryWriteService;
    private final MemberReadService memberReadService;

    @Value("${cloud.aws.s3.basic}")
    private String basicImageUrl;

    public void saveMember(SignUpRequest request){
        if(memberRepository.existsByEmail(request.getEmail())){
            throw new CMissingDataException("이미 존재하는 이메일입니다.");
        }

        String cryptedPassword = passwordEncoder.encode(request.getPassword());
        log.info("cryptedPassword: {}", cryptedPassword);

        if (cryptedPassword == null) {
            throw new CMissingDataException("비밀번호 암호화를 실패했습니다.");
        }

        Member member = new Member(request, cryptedPassword, basicImageUrl);

        log.info("member: {}", member.toString());
        memberRepository.save(member);

        //비동기 처리 ㄱㄱ
        categoryWriteService.createCategory(request.getEmail());
    }

    public void updateMember(UpdateMemberRequest request, CustomUser customUser, MultipartFile file)
        throws IOException{
        if(memberRepository.existsByNickname(request.getNickname())){
            throw new CInvalidDataException("중복되는 닉네임 입니다.");
        }

        Member member = memberReadService.getById(customUser.getMemberId());

        String profileImg = file.getOriginalFilename();
        String memberImg = member.getProfileImg();
        log.info("memberImg: {}, profileImg: {}", memberImg, profileImg);
        if(isSame(memberImg, profileImg)){
            member.update(request);
        } else {
            profileImg = s3Service.upload(file).orElseThrow(CMissingDataException::new);
            if(!memberImg.equals(basicImageUrl)) s3Service.deleteImage(memberImg);
            member.update(request, profileImg);
        }
        log.info("memberImg: {}, profileImg: {}", memberImg, profileImg);
    }

    public void deleteMember(CustomUser customUser){
        Member member = memberReadService.getById(customUser.getMemberId());

        if(!member.getProfileImg().equals(basicImageUrl)) {
            s3Service.deleteImage(member.getProfileImg());
        }

        memberRepository.deleteById(customUser.getMemberId());
    }

    private boolean isSame(String origin, String update) {
        return origin.substring(93).equals(update);
    }
}
