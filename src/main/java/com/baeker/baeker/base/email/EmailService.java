package com.baeker.baeker.base.email;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {

    private JavaMailSender sender;
    private static final String FROM_ADDRESS = "shdrnrhd113@gmail.com";

    public void mailSend(MailDto dto) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(dto.getAddress());
        msg.setSubject(dto.getSubject());
        msg.setText(dto.getText());
        sender.send(msg);
    }
}
