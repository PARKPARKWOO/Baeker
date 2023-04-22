package com.baeker.baeker.study;

import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.embed.BaekJoon;
import com.baeker.baeker.member.embed.Programmers;
import com.baeker.baeker.myStudy.MyStudy;
import com.baeker.baeker.studyRule.StudyRule;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor(access = PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Study {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
    private String about;
    private String leader;
    private Integer capacity;
    private Integer xp;

    @CreatedDate
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;

    @Embedded
    private BaekJoon baekJoon;
    @Embedded
    private Programmers programmers;

    @Builder.Default
    @OneToMany(mappedBy = "study")
    private List<MyStudy> myStudies = new ArrayList<>();


    //-- create method --//
    public static Study createStudy(String name, String about, Integer capacity, Member member) {
        return Study.builder()
                .name(name)
                .about(about)
                .leader(member.getNickName())
                .capacity(capacity)
                .xp(0)
                .build();
    }


    //-- business logic --//

    // 이름, 소개, 최대 인원 변경 //
    public Study modifyStudy(String name, String about, Integer capacity) {
        return this.toBuilder()
                .name(name)
                .about(about)
                .capacity(capacity)
                .modifyDate(LocalDateTime.now())
                .build();
    }

    // 리더 변경 //
    public Study modifyLeader(String leader) {
        return this.toBuilder()
                .leader(leader)
                .modifyDate(LocalDateTime.now())
                .build();
    }

    // 경험치 상승 //
    public void xpUp(Integer addXp) {
        this.xp += addXp;
    }

    // 해결한 문제 수 생성 //
    public void createSolve(BaekJoon baekJoon) {
        this.baekJoon = baekJoon;
    }

    // 해결한 문제 수 업데이트 //
    public void updateSolve(BaekJoon addedSolved) {
        BaekJoon beakJoon = BaekJoon.builder()
                .bronze(addedSolved.getBronze() + baekJoon.getBronze())
                .sliver(addedSolved.getSliver() + baekJoon.getSliver())
                .gold(addedSolved.getGold() + baekJoon.getGold())
                .platinum(addedSolved.getPlatinum() + baekJoon.getPlatinum())
                .diamond(addedSolved.getDiamond() + baekJoon.getDiamond())
                .ruby(addedSolved.getRuby() + baekJoon.getRuby())
                .build();

        this.baekJoon = beakJoon;
    }
}
