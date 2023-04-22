package com.baeker.baeker.myStudy;

import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.embed.BaekJoon;
import com.baeker.baeker.study.Study;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor(access = PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MyStudy {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String msg;

    @Enumerated(EnumType.STRING)
    private StudyStatus status;

    @CreatedDate
    private LocalDateTime joinDate;


    @ManyToOne(fetch = LAZY)
    private Member member;

    @ManyToOne(fetch = LAZY)
    private Study study;


    //-- create method --//

    // 새로운 스터디 만들 때 //
    public static MyStudy createNewStudy(Member member, Study study) {
        MyStudy myStudy = create(member, study);
        myStudy.status = StudyStatus.MEMBER;

        member.getMyStudies().add(myStudy);
        study.getMyStudies().add(myStudy);

        return myStudy;
    }

    // 스터디 가입할 때 //
    protected static MyStudy joinStudy(Member member, Study study, String msg) {
        MyStudy myStudy = create(member, study);
        myStudy.status = StudyStatus.PENDING;
        myStudy.msg = msg;

        member.getMyStudies().add(myStudy);
        study.getMyStudies().add(myStudy);

        return myStudy;
    }

    // 스터디로 초대할 때 //
    protected static MyStudy inviteStudy(Member member, Study study, String msg) {
        MyStudy myStudy = create(member, study);
        myStudy.status = StudyStatus.INVITING;
        myStudy.msg = msg;

        member.getMyStudies().add(myStudy);
        study.getMyStudies().add(myStudy);

        return myStudy;
    }

    // my study 생성 //
    private static MyStudy create(Member member, Study study) {
        MyStudy myStudy = new MyStudy();
        myStudy.member = member;
        myStudy.study = study;

        return myStudy;
    }


    //-- business logic --//

    // 가입, 초대 신청 승인 //
    protected BaekJoon accept() {
        BaekJoon baekJoon = this.member.getBaekJoon();

        if (member.getBaekJoon() != null)
            this.study.updateSolve(baekJoon);

        this.status = StudyStatus.MEMBER;
        this.msg = null;
        return baekJoon;
    }
}
