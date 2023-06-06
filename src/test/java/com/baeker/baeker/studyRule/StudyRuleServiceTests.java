package com.baeker.baeker.studyRule;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.MemberService;
import com.baeker.baeker.member.embed.BaekJoonDto;
import com.baeker.baeker.member.form.MemberJoinForm;
import com.baeker.baeker.rule.Rule;
import com.baeker.baeker.rule.RuleForm;
import com.baeker.baeker.rule.RuleService;
import com.baeker.baeker.study.Study;
import com.baeker.baeker.study.StudyService;
import com.baeker.baeker.study.form.StudyCreateForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class StudyRuleServiceTests {

    @Autowired
    StudyRuleRepository studyRuleRepository;

    @Autowired
    StudyRuleService studyRuleService;

    @Autowired
    RuleService ruleService;
    @Autowired
    MemberService memberService;
    @Autowired
    StudyService studyService;

    private Member create(String username, String name) {
        MemberJoinForm form = new MemberJoinForm(username, name, "", "1234", "1234", "");
        Member member = memberService.join(form).getData();

        RsData<Member> memberRsData = memberService.connectBaekJoon(member, name);
        return member;
    }

    private RsData<Study> createStudy(String name, String about, Integer capacity, Member member) {
        StudyCreateForm form = new StudyCreateForm(name, about, capacity);
        return studyService.create(form, member);
    }

    @Test
    @DisplayName(value = "생성 메서드 테스트")
    void createTests() {
        //
        RuleForm ruleForm = new RuleForm("name", "about", "1", "3","provider", "gold");
        Member member = create("wy9295", "wy9295");
        Study study = createStudy("study", "study", 1, member).getData();
        StudyRuleForm studyRuleForm = new StudyRuleForm("aaaa", "소개", study.getId());
        Rule rule = ruleService.create(ruleForm).getData();

        //생성 메서드 //
        RsData<StudyRule> rsData = studyRuleService.create(studyRuleForm, rule.getId(), study);
        StudyRule studyRule = rsData.getData();
        assertThat(studyRule.getName()).isEqualTo("aaaa");
        System.out.println(rsData.getMsg());

        // 삭제 메서드 //
        Optional<StudyRule> opDelete = studyRuleRepository.findById(1L);
        opDelete.ifPresent(value -> studyRuleService.delete(value, "wy9295", "wy9295"));
        assertThat(studyRuleService.getStudyRule(1L).getData()).isNull();
    }

    @Test
    @DisplayName("수정 메서드")
    void modifyTests() {
        //
        RuleForm ruleForm = new RuleForm("name", "about", "1", "3","provider", "gold");
        Member member = create("wy9295", "wy9295");
        Study study = createStudy("study", "study", 1, member).getData();
        StudyRuleForm studyRuleForm = new StudyRuleForm("aaaa", "소개", study.getId());
        Rule rule = ruleService.create(ruleForm).getData();
        //생성 메서드 //

        RsData<StudyRule> rsData = studyRuleService.create(studyRuleForm, rule.getId(), study);
        StudyRule studyRule = rsData.getData();
        assertThat(studyRule.getName()).isEqualTo("aaaa");

        String leader = studyRule.getStudy().getLeader();

        //수정 메서드 //
        StudyRuleForm ruleForm1 = new StudyRuleForm("wy9295", "소개2", study.getId());
        Optional<StudyRule> optionalRule = studyRuleRepository.findByName("aaaa");
        if (optionalRule.isEmpty()) {
            System.out.println("실패 테스트임 !! 값이없다!!!!!!");
        }

        StudyRule studyRule1 = optionalRule.get();
        studyRuleService.modify(studyRule1, ruleForm1);

        assertThat(studyRule1.getName()).isEqualTo("wy9295");
    }


    @Test
    @DisplayName("삭제 메서드 테스트")
    void deleteTests() {
        //
        RuleForm ruleForm = new RuleForm("name", "about", "1", "3","provider", "gold");
        Member member = create("wy9295", "wy9295");
        Study study = createStudy("study", "study", 1, member).getData();
        StudyRuleForm studyRuleForm = new StudyRuleForm("aaaa", "소개", study.getId());
        Rule rule = ruleService.create(ruleForm).getData();
        //생성 메서드 //
        RsData<StudyRule> rsData = studyRuleService.create(studyRuleForm, rule.getId(), study);
        StudyRule studyRule = rsData.getData();
        assertThat(studyRule.getName()).isEqualTo("aaaa");
        System.out.println(rsData.getMsg());

        // 삭제 메서드 //
        Optional<StudyRule> opDelete = studyRuleRepository.findById(1L);
        opDelete.ifPresent(value -> studyRuleService.delete(value, "wy9295", "wy9295"));
        assertThat(studyRuleService.getStudyRule(1L).getData()).isNull();
    }
}
