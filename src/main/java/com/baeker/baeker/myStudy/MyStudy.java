package com.baeker.baeker.myStudy;

import com.baeker.baeker.member.Member;
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
@ToString
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor(access = PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MyStudy {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StudyStatus status;

    @CreatedDate
    private LocalDateTime joinDate;


    @ManyToOne(fetch = LAZY, cascade = ALL)
    private Member member;

    @ManyToOne(fetch = LAZY)
    private Study study;


    //-- create method --//

    // 새로운 스터디 만들 때 //
    public static MyStudy createNewStudy(Member member, Study study){
        MyStudy myStudy = create(member, study);
        myStudy.status = StudyStatus.MEMBER;

        member.getMyStudies().add(myStudy);
        study.getMyStudies().add(myStudy);

        return myStudy;
    }

    // 스터디 가입할 때 //
    protected static MyStudy joinStudy(Member member, Study study) {
        MyStudy myStudy = create(member, study);
        myStudy.status = StudyStatus.PENDING;

        member.getMyStudies().add(myStudy);
        study.getMyStudies().add(myStudy);

        return myStudy;
    }

    // 스터디로 초대할 때 //
    protected static MyStudy inviteStudy(Member member, Study study) {
        MyStudy myStudy = create(member, study);
        myStudy.status = StudyStatus.INVITING;

        member.getMyStudies().add(myStudy);
        study.getMyStudies().add(myStudy);

        return myStudy;
    }

    // my study 생성 //
    private static MyStudy create(Member member, Study study) {
        return builder()
                .member(member)
                .study(study)
                .build();
    }


    //-- business logic --//

    // 가입, 초대 신청 승인 //
    protected void accept() {
        this.status = StudyStatus.MEMBER;
    }
}