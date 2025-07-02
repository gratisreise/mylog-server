package com.mylog.service;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.member.SignUpRequest;
import com.mylog.model.dto.member.UpdateMemberRequest;
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
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final CategoryService categoryService;

    @Value("${cloud.aws.s3.basic}")
    private String basicImageUrl;

    // 회원가입
    @Transactional
    public void saveMember(SignUpRequest request){
        if(memberRepository.existsByEmail(request.getEmail())){
            throw new CMissingDataException("이미 존재하는 이메일입니다.");
        }

        String cryptedPassword = passwordEncoder.encode(request.getPassword());
        log.info("cryptedPassword: {}", cryptedPassword);

        if (cryptedPassword == null) {
            throw new CMissingDataException("비밀번호 암호화를 실패했습니다.");
        }

        Member member = Member.builder()
            .email(request.getEmail())
            .memberName(request.getMemberName())
            .password(cryptedPassword)
            .nickname(request.getEmail())
            .provider(OauthProvider.LOCAL)
            .providerId(request.getEmail() + OauthProvider.LOCAL)
            .profileImg(basicImageUrl)
            .build();

        log.info("member: {}", member.toString());
        memberRepository.save(member);

        categoryService.createCategory(request.getEmail());
    }


    //회원정보조회
    public Member getMember(CustomUser customUser){
        return memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
    };

    //회원정보수정
    @Transactional
    public void updateMember(UpdateMemberRequest request, CustomUser customUser, MultipartFile file)
        throws IOException{
        //닉네임 중복확인
        if(memberRepository.existsByNickname(request.getNickname())){
            throw new CInvalidDataException("중복되는 닉네임 입니다.");
        }

        Member member = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);

        //새사진
        String profileImg = file.getOriginalFilename();
        //기존사진
        String memberImg = member.getProfileImg();
        log.info("memberImg: {}, profileImg: {}", memberImg, profileImg);
        if(isSame(memberImg, profileImg)){ // 이미지가 같을 때
            member.update(request);
        } else { //이미지가 다를 때
            profileImg = s3Service.upload(file).orElseThrow(CMissingDataException::new);
            if(!memberImg.equals(basicImageUrl)) s3Service.deleteImage(memberImg);
            member.update(request, profileImg);
        }
        log.info("memberImg: {}, profileImg: {}", memberImg, profileImg);
    };

    // 사용자 정보삭제
    @Transactional
    public void deleteMember(CustomUser customUser){
        Member member = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);

        if(!member.getProfileImg().equals(basicImageUrl)) {
            s3Service.deleteImage(member.getProfileImg());
        }

        memberRepository.deleteById(customUser.getMemberId());
    };

    private boolean isSame(String origin, String update) {
        return origin.substring(93).equals(update);
    }

}
