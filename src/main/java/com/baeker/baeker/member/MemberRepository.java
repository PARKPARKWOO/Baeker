package com.baeker.baeker.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUsername(String username);

    Optional<Member> findByNickName(String name);

    Page<Member> findAll(Pageable pageable);

    Optional<Member> findByBaekJoonName(String baekJoonName);
}
