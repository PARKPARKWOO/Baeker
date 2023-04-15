package com.baeker.baeker.home;

import com.baeker.baeker.base.request.Rq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final Rq rq;

    @GetMapping("/")
    public String home() {
        log.info("홈페이지 요청 확인");

        if (rq.isLogin()) {
            log.info("로그인 회원 입장 user id = {}", rq.getMember().getUserId());
            return "redirect:/member/profile";
        }

        log.info("로그아웃 회원 입장");
        return "member/home";
    }
}
