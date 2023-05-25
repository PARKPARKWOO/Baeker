package com.baeker.baeker.rule;

import com.baeker.baeker.base.entity.BaseEntity;
import com.baeker.baeker.studyRule.StudyRule;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;


@Entity
@SuperBuilder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Rule extends BaseEntity {

    private String name;

    private String about;

    private String provider;

    private int xp;

    private int count;
    private String difficulty;


    @OneToMany(mappedBy = "rule", cascade = ALL)
    @Builder.Default
    private List<StudyRule> studyRules = new ArrayList<>();

}
