package com.baeker.baeker.base.initDB;

import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.MemberService;
import com.baeker.baeker.member.form.MemberJoinForm;
import com.baeker.baeker.myStudy.MyStudy;
import com.baeker.baeker.myStudy.MyStudyService;
import com.baeker.baeker.rule.RuleForm;
import com.baeker.baeker.rule.RuleService;
import com.baeker.baeker.study.Study;
import com.baeker.baeker.study.StudyService;
import com.baeker.baeker.study.form.StudyCreateForm;
import com.baeker.baeker.studyRule.StudyRuleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class InitDB {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.initData();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final MemberService memberService;
        private final StudyService studyService;
        private final MyStudyService myStudyService;

        private final RuleService ruleService;
        private final StudyRuleService studyRuleService;

        public void initData() {

            // 맴버 "알파" 생성
            MemberJoinForm memberForm0 = new MemberJoinForm("user1", "알파", "안녕하세요 알파입니다.", "1234", "1234", 100);
            Member member0 = memberService.join(memberForm0).getData();

            // 알파가 스터디 "Baeker lover" 생성
            StudyCreateForm studyFrom0 = new StudyCreateForm("Baeker lover", "Provident cupiditate voluptatem et in.", 10);
            Study study0 = studyService.create(studyFrom0, member0).getData();
            myStudyService.create(member0, study0);

            // init 맴버 8명 생성후 "Baeker lover" 에 가입
            for (int i = 2; i < 10; i++) {
                Member memberA = memberService.join(new MemberJoinForm("user" + i, "member" + i, "hello" + i, "1234", "1234", i * 5)).getData();
                MyStudy myStudy = myStudyService.join(memberA, study0, "hi").getData();
                myStudyService.accept(myStudy);
            }


            // 맴버 "베타" 생성
            MemberJoinForm memberForm1 = new MemberJoinForm("user11", "베타", "안녕하세요 베타입니다.", "1234", "1234", 200);
            Member member1 = memberService.join(memberForm1).getData();

            // 베타가 스터디 "Study Baeker" 생성
            StudyCreateForm studyFrom1 = new StudyCreateForm("Study Baeker", "Quaerat voluptatem et cupiditate in.", 10);
            Study study1 = studyService.create(studyFrom1, member1).getData();
            myStudyService.create(member1, study1);

            // 알파가 "Study Baeker" 가입
            MyStudy myStudy = myStudyService.join(member0, study1, "hi").getData();
            myStudyService.accept(myStudy);

            // rule 목록확인
            RuleForm ruleForm = new RuleForm("이름", "소개",  "1","백준", "GOLD");

            for (int i = 0; i < 15; i++) {
                ruleService.create(ruleForm);
            }


        }
    }

}
