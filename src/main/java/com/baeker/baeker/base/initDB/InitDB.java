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
        initService.init_alpha_and_study();
        initService.init_beta_and_study();
        initService.init_dummy_user();
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


        public void init_alpha_and_study() {

            // user 알파와 알파의 스터디 생성
            Member alpha = createMember("user1", "알파", "안녕하세요 알파입니다.", 100);
            Study study = createStudy("알파의 스터디", "Provident cupiditate voluptatem et in.", 10, alpha);
            createStudy("알파알파", "Provident cupiditate voluptatem et in.", 10, alpha);

            // 더미 user 7명 생성후 알파 스터디 가입
            for (int i = 3; i < 10; i++) {
                Member member = createMember("user" + i, "member" + i, "안녕하세요" + i, i * 5);
                MyStudy myStudy = myStudyService.join(member, study, "hi").getData();
                myStudyService.accept(myStudy);
            }
        }

        public void init_beta_and_study() {

            // user 베타와 베타의 스터디 생성
            Member beta = createMember("user2", "베타", "안녕하세요 베타입니다.", 200);
            Study study1 = createStudy("베타의 스터디", "Quaerat voluptatem et cupiditate in.", 10, beta);
            Study study2 = createStudy("베타베타", "Quaerat voluptatem et cupiditate in.", 10, beta);

            // 베타가 알파 초대
            Member alpha = memberService.getMember(1L).getData();
            myStudyService.invite(beta, alpha, study1, "초대합니다.");

            // 알파가 스터디 가입 요청
            myStudyService.join(alpha, study2, "가입 원해요.");
        }

        public void init_dummy_user() {

            // 스터디가 없는 더미 유저 5명 생성
            for (int i = 0; i < 5; i++)
                createMember("dummy" + i, "dummy" + i, "안녕하세요" + i, i * 5);
        }

        public void initData() {

            // rule 목록확인
            RuleForm ruleForm = new RuleForm("이름", "소개", "1", "백준", "GOLD");

            for (int i = 0; i < 15; i++) {
                ruleService.create(ruleForm);
            }
        }

        private Member createMember(String username, String nickName, String about, Integer img) {
            MemberJoinForm form = new MemberJoinForm(username, nickName, about, "1234", "1234", img);
            return memberService.join(form).getData();
        }

        private Study createStudy(String name, String about, Integer capacity, Member member) {
            StudyCreateForm form = new StudyCreateForm(name, about, capacity);
            Study study = studyService.create(form, member).getData();
            myStudyService.create(member, study);
            return study;
        }
    }

}
