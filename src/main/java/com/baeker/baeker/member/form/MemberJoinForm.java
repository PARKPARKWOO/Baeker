package com.baeker.baeker.member.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberJoinForm {

    @NotBlank
    @Size(min = 4, max = 20)
    private String username;
    @NotBlank
    @Size(min = 2, max = 10)
    private String nickName;

    @Size(max = 20)
    private String about;

    @NotBlank
    @Size(min = 4, max = 20)
    private String password;
    @NotBlank
    @Size(min = 4, max = 20)
    private String password2;

    private String profileImg;
}
