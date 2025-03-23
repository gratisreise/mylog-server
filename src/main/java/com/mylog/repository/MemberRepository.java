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

    Optional<Member> findByEmailAndProvider(String email, OauthProvider provider);
}
