package com.baeker.baeker.member;

import com.baeker.baeker.member.embed.BaekJoon;
import com.baeker.baeker.member.embed.Programmers;
import com.baeker.baeker.myStudy.MyStudy;
import com.baeker.baeker.study.Study;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String studyId;
    private String nickName;
    private String about;
    private Integer profileImg;
    private String password;
    private String provider;
    private String token;

    @CreatedDate
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;

    @Embedded
    private BaekJoon baekJoon;
    @Embedded
    private Programmers programmers;


    @Builder.Default
    @OneToMany(mappedBy = "member")
    private List<MyStudy> myStudies = new ArrayList<>();


    //-- crate method --//
    protected static Member createMember(String provider, String username, String name, String about, String password, Integer profileImg) {
        return Member.builder()
                .provider(provider)
                .username(username)
                .nickName(name)
                .about(about)
                .password(password)
                .profileImg(profileImg)
                .build();
    }

    //-- business logic --//

    // name, about, profileImg 수정 //
    protected Member modifyMember(String name, String about, Integer img) {
        return this.toBuilder()
                .nickName(name)
                .about(about)
                .profileImg(img)
                .modifyDate(LocalDateTime.now())
                .build();
    }


    // BaekJoon 생성 //
    protected BaekJoon createSolve(BaekJoon baekJoon) {
        this.baekJoon = baekJoon;

        if (myStudies.size() != 0)
            for (MyStudy myStudy : myStudies) {
                Study study = myStudy.getStudy();

                if (study.getBaekJoon() == null)
                    study.createSolve(baekJoon);
                else
                    study.updateSolve(baekJoon);
            }

        return baekJoon;
    }

    // BaekJoon 최신화 , Study BaekJoon 합산 //
    protected BaekJoon updateSolve(BaekJoon baekJoon) {
        BaekJoon addedSolved = BaekJoon.builder()
                .bronze(baekJoon.getBronze() - this.baekJoon.getBronze())
                .sliver(baekJoon.getSliver() - this.baekJoon.getSliver())
                .gold(baekJoon.getGold() - this.baekJoon.getGold())
                .platinum(baekJoon.getPlatinum() - this.baekJoon.getPlatinum())
                .diamond(baekJoon.getDiamond() - this.baekJoon.getDiamond())
                .ruby(baekJoon.getRuby() - this.baekJoon.getRuby())
                .build();

        this.baekJoon = baekJoon;
        if (myStudies.size() != 0)
            for (MyStudy myStudy : myStudies)
                myStudy.getStudy().updateSolve(addedSolved);

        return addedSolved;
    }


    //-- create authorize --//
    public List<? extends GrantedAuthority> getGrantedAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // 모든 회원에게 member 권한 부여 //
        grantedAuthorities.add(new SimpleGrantedAuthority("member"));

        // admin 권한 부여 //
        if ("admin".equals(username))
            grantedAuthorities.add(new SimpleGrantedAuthority("admin"));

        return grantedAuthorities;
    }
}
