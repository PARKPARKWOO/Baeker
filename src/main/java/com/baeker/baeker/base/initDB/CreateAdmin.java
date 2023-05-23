package com.baeker.baeker.base.initDB;

import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.MemberService;
import com.baeker.baeker.member.form.MemberJoinForm;
import com.baeker.baeker.solvedApi.SolvedApiService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class CreateAdmin {

    private final InitService initService;

    @PostConstruct
    public void init() throws IOException, ParseException {
        initService.createAdmin();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final MemberService memberService;
        private final SolvedApiService solvedApiService;


        public void createAdmin() throws IOException, ParseException {

            createMember("admin", "운영자", "", "https://avatars.dicebear.com/api/avataaars/600.svg", "sunnight9507");
        }

        private Member createMember(String username, String nickName, String about, String img, String baekJoonName) throws IOException, ParseException {
            MemberJoinForm form = new MemberJoinForm(username, nickName, about, "1234", "1234", img);
            Member member = memberService.join(form).getData();

            if (!solvedApiService.findUser(baekJoonName))
                throw new IllegalArgumentException("존재하지 않는 ID");

            memberService.connectBaekJoon(member, baekJoonName);
            solvedApiService.getSolvedCount(member.getId());
            return member;
        }
    }
}
