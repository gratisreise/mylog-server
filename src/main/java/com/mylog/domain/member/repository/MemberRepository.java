package com.mylog.domain.member.repository;

import com.mylog.common.enums.OauthProvider;
import com.mylog.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByProviderId(String providerId);
    // provider와 providerId로 회원 조회
    Optional<Member> findByProviderAndProviderId(OauthProvider provider, String providerId);

    Optional<Member> findByNickname(String author);

    boolean existsByProviderAndProviderId(OauthProvider provider, String providerId);
}
