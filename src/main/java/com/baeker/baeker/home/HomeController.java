package com.baeker.baeker.home;

import com.baeker.baeker.base.request.Rq;
import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.MemberService;
import com.baeker.baeker.member.form.MemberLoginForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final Rq rq;

    //-- welcome page redirect --//
    @GetMapping("/")
    public String home(
            MemberLoginForm form
    ) {
        log.info("홈페이지 요청 확인");

        if (rq.isLogin()) {
            log.info("로그인 회원 입장 user id = {}", rq.getMember().getUsername());
            return "redirect:/member/profile/rank";
        }

        log.info("로그아웃 회원 입장");
        return "member/home";
    }

    @GetMapping("/chat")
    public String chat() {
        return "layout/chat";
    }

}
