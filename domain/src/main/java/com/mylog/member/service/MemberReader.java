package com.mylog.member.service;


import com.mylog.enums.OauthProvider;
import com.mylog.exception.common.CDuplicatedException;
import com.mylog.exception.common.CMissingDataException;
import com.mylog.exception.common.CUnDeletedException;
import com.mylog.exception.auth.AuthError;
import com.mylog.exception.auth.LoginFailedException;
import com.mylog.exception.common.CommonError;
import com.mylog.member.entity.Member;
import com.mylog.member.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberReader {
    private final MemberRepository memberRepository;

    public Member getById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new CMissingDataException(CommonError.MEMBER_IS_EMPTY));
    }

    public void isDeleted(Long memberId) {
        if(memberRepository.existsById(memberId)){
            throw new CUnDeletedException(CommonError.FAILED_DELETE_MEMBER);
        }
    }


    public Optional<Member> findByProviderAndProviderId(OauthProvider provider, String providerId){
        return memberRepository.findByProviderAndProviderId(provider, providerId);

    }
    //    public MemberResponse getMember(CustomUser customUser){
//        Member member = memberRepository.findById(customUser.getMemberId())
//            .orElseThrow(CMissingDataException::new);
//        return new MemberResponse(member);
//    }
//

//
//    public Member getByNickname(String author) {
//        return memberRepository.findByNickname(author).orElseThrow(CMissingDataException::new);
//    }
//


    public Member getByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new CMissingDataException(AuthError.INVALID_LOGIN_INPUT));
    }

    public void isDuplicated(String email) {
        if(memberRepository.existsByEmail(email)){
            throw new CDuplicatedException(AuthError.DUPLICATED_EMAIL);
        }
    }

//    public Member getByCustomUser(CustomUser customUser) {
//        return memberRepository.findById(customUser.getMemberId())
//            .orElseThrow(CMissingDataException::new);
//    }
}
