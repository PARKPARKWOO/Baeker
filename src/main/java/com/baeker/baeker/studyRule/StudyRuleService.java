package com.baeker.baeker.studyRule;

import com.baeker.baeker.base.email.EmailService;
import com.baeker.baeker.base.email.MailDto;
import com.baeker.baeker.base.request.Rq;
import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.myStudy.MyStudy;
import com.baeker.baeker.rule.Rule;
import com.baeker.baeker.rule.RuleForm;
import com.baeker.baeker.rule.RuleService;
import com.baeker.baeker.study.Study;
import com.baeker.baeker.study.StudyService;
import com.baeker.baeker.study.snapshot.StudySnapShotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StudyRuleService {

    private final StudyRuleRepository studyRuleRepository;

    private final StudyService studyService;

    private final RuleService ruleService;
    private final StudySnapShotRepository studySnapShotRepository;
    private final EmailService emailService;


    /**
     * 생성
     */

    @Transactional
    public RsData<StudyRule> create(StudyRuleForm studyRuleForm, Long ruleId, Study study) {
        Rule rule = ruleService.getRule(ruleId).getData();

        StudyRule studyRule = StudyRule.builder()
                .name(studyRuleForm.getName())
                .about(studyRuleForm.getAbout())
                .rule(rule)
                .study(study)
                .build();
        studyRule.getRule().getStudyRules().add(studyRule);
        studyRule.getStudy().getStudyRules().add(studyRule);
        studyRuleRepository.save(studyRule);
        return RsData.of("S-1", "스터디 규칙 생성완료.", studyRule);
    }

    /**
     * 수정
     */

    @Transactional
    public void modify(StudyRule studyRule, StudyRuleForm studyRuleForm) {
        StudyRule modifyRule = studyRule.toBuilder()
                .name(studyRuleForm.getName())
                .about(studyRuleForm.getAbout())
                .build();
        studyRuleRepository.save(modifyRule);
        RsData.of("S-1", "수정되었습니다.", modifyRule);
    }

    public void setForm(StudyRule studyRule, StudyRuleForm studyRuleForm) {
        studyRuleForm.setName(studyRule.getName());
        studyRuleForm.setAbout(studyRule.getAbout());
    }

    /**
     * 삭제
     */

    @Transactional
    public RsData<StudyRule> delete(StudyRule studyRule, String leader, String user) {
        if (leader.equals(user)) {
            studyRule.getRule().getStudyRules().remove(studyRule);
            studyRuleRepository.delete(studyRule);
            return RsData.of("S-1", "삭제되었습니다");
        } else {
            return RsData.failOf(studyRule);
        }
    }

    /**
     * 조회
     */

    public RsData<StudyRule> getStudyRule(Long id) {
        Optional<StudyRule> rs = studyRuleRepository.findById(id);
        return rs.map(studyRule -> RsData.of("S-1", "StudyRule 조회 성공", studyRule))
                .orElseGet(() -> RsData.of("F-1", "StudyRule 조회 실패"));
    }

    public RsData<StudyRule> getStudyRule(String name) {
        Optional<StudyRule> rs = studyRuleRepository.findByName(name);
        return rs.map(studyRule -> RsData.of("S-1", "StudyRule 조회", studyRule))
                .orElseGet(() -> RsData.of("F-1", "StudyRule 조회 실패"));
    }

    // StudyRuleId -> StudyId
    public Long getStudyId(Long id) {
        return getStudyRule(id).getData().getStudy().getId();
    }

    public List<StudyRule> getAll() {
        return studyRuleRepository.findAll();
    }




    /**
     * 검증
     */

    public RsData<Study> verificationLeader(Rq rq, Long id) {
        RsData<Study> rsData = studyService.getStudy(id);
        if (rsData.isSuccess()) {
            if (rsData.getData().getLeader().equals(rq.getMember().getNickName())) {
                return RsData.of("S-1", "리더 입니다." , rsData.getData());
            }
        }
        return RsData.of("F-1" , "리더가 아닙니다.");
    }

    /**
     * xp 반환
     */
    public Integer getXp(Long id) {
        StudyRule studyRule = getStudyRule(id).getData();
        return studyRule.getRule().getXp();
    }

    public Integer getXp(StudyRule studyRule) {
        return getXp(studyRule.getId());
    }

    public void setMission(Long id, boolean mission) {
        StudyRule studyRule = getStudyRule(id).getData();
        studyRule.setMission(mission);
    }

    /**
     *
     * @param id = studyRuleId
     * else 에는 kakao 메시지 발송 기능 추가 필요
     */
    public void whenstudyEventType(Long id) {
        StudyRule studyRule = getStudyRule(id).getData();
        String studyName = studyRule.getStudy().getName();
        int count = 0;
        int ruleCount = studyRule.getRule().getCount();
        String difficulty = studyRule.getRule().getDifficulty();

        switch (difficulty) {
            case "BRONZE" -> count = studySnapShotRepository.findByStudyName(studyName).get(6).getBronze();
            case "SILVER" -> count = studySnapShotRepository.findByStudyName(studyName).get(6).getSliver();
            case "GOLD" -> count = studySnapShotRepository.findByStudyName(studyName).get(6).getGold();
            case "PLATINUM" -> count = studySnapShotRepository.findByStudyName(studyName).get(6).getPlatinum();
            case "DIAMOND" -> count = studySnapShotRepository.findByStudyName(studyName).get(6).getDiamond();
            case "RUBY" -> count = studySnapShotRepository.findByStudyName(studyName).get(6).getRuby();
        }

        if (count >= ruleCount) {
            setMission(studyRule.getId(), true);
            studyService.xpUp(studyRule.getRule().getXp(), studyRule.getStudy().getId());
            log.info("study xp ++");
        } else {
            setMission(studyRule.getId(), false);
            List<MyStudy> myStudies = studyRule.getStudy().getMyStudies();
            for (MyStudy myStudy : myStudies) {
                emailService.mailSend(new MailDto(myStudy.getMember().getEmail(),
                        String.format("%s 미션 실패 메일입니다.", studyName), "오늘 하루도 화이팅 입니다 :)"));
            }
        }


    }
}
