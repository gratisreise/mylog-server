package com.mylog.repository;

import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmailAndProvider(String email, OauthProvider provider);

    // provider와 providerId로 회원 조회
    Optional<Member> findByProviderAndProviderId(OauthProvider provider, String providerId);

    // provider와 providerId 조합의 존재 여부 확인
    boolean existsByProviderAndProviderId(OauthProvider provider, String providerId);

}
