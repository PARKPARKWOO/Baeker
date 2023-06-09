package com.baeker.baeker.base.messge.kakao.service;

import com.baeker.baeker.base.messge.kakao.dto.KakaoMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomMessageService {

    private final KakaoMessageService service;

    public boolean sendMessage(String accessToken) {
        KakaoMessageDto myMsg = new KakaoMessageDto();
        myMsg.setBtnTitle("버튼");
        myMsg.setMobileUrl("");
        myMsg.setObjType("text");
        myMsg.setWebUrl("");
        myMsg.setText("메시지 테스트입니다.");

        return service.sendMsgToMe(accessToken, myMsg);
    }
}
