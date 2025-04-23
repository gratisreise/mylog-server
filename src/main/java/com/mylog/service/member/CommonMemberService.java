package com.mylog.service.member;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommonMemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    @Value("aws.s3.basic")
    private String basicImageUrl;

    // 회원가입
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
            .providerId(request.getEmail()+OauthProvider.LOCAL)
            .profileImg(basicImageUrl)
            .build();

        log.info("member: {}", member.toString());
        memberRepository.save(member);
    }


    //사용자 정보조회
    public Member getMember(CustomUser customUser){
        return memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
    };

    //사용자 정보수정
    public void updateMember(UpdateMemberRequest request, CustomUser customUser, MultipartFile file)
        throws IOException{
        //닉네임 중복확인
        if(memberRepository.existsByNickname(request.getNickname())){
            throw new CInvalidDataException("중복되는 닉네임 입니다.")
        }

        //프로파일 이미지 확인
        String profileImg = file.getOriginalFilename();
        Member member = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
        if(isSame(member.getProfileImg(), profileImg)){
            member.update(request);
        } else {
            profileImg = s3Service.upload(file).orElseThrow(CMissingDataException::new);
            member.update(request, profileImg);
        }
    };

    // 사용자 정보삭제
    public void deleteMember(CustomUser customUser){

    };

    private boolean isSame(String origin, String update) {
        return origin.substring(57).equals(update);
    }

}
