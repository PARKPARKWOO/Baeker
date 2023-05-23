package com.baeker.baeker.base.email;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService service;

    @PostMapping("/mail")
    public String execMail(@RequestBody @Valid MailDto dto) {
        log.info("요청확인");
        service.mailSend(dto);
        log.info("전송완료");
        return "good";
    }
}
