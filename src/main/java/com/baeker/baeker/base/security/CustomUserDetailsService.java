package com.baeker.baeker.base.security;

import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository repository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member member = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "username(%s) not found".formatted(username)
                ));

        return new User(member.getUsername(), member.getPassword(), member.getGrantedAuthorities());
    }
}
