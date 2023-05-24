package com.baeker.baeker.rule;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleForm {
    @NotBlank
    @Size(max = 10, min = 1)
    private String name;

    @Size(max = 30)
    private String about;

    @NotEmpty
    @Size(min = 1, max = 10)
    private int xp;

    @NotEmpty
    private int count;

    @NotBlank
    private String provider;

    @NotEmpty
    private String difficulty;
}
