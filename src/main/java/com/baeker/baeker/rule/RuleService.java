package com.baeker.baeker.rule;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.global.exception.NotFoundData;
import com.baeker.baeker.global.exception.NumberInputException;
import com.baeker.baeker.studyRule.StudyRule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RuleService {

    private final RuleRepository ruleRepository;

    /**
     * 생성
     */
    @Transactional
    public RsData<Rule> create(RuleForm ruleForm) {
        Rule rule = Rule.builder()
                .name(ruleForm.getName())
                .about(ruleForm.getAbout())
                .xp(Integer.parseInt(ruleForm.getXp()))
                .count(Integer.parseInt(ruleForm.getCount()))
                .provider(ruleForm.getProvider())
                .difficulty(ruleForm.getDifficulty())
                .build();
        ruleRepository.save(rule);
        return RsData.of("S-1", "Rule 생성 완료", rule);
    }

    /**
     * 수정
     * setForm 은 기존 작성되어있는 내용을 넣어주는 메서드
     */

    @Transactional
    public void modify(Long ruleId, RuleForm ruleForm) {
        Rule rule = getRule(ruleId).getData();

        Rule rule1 = rule.toBuilder()
                .name(ruleForm.getName())
                .about(ruleForm.getAbout())
                .provider(ruleForm.getProvider())
                .xp(Integer.parseInt(ruleForm.getXp()))
                .count(Integer.parseInt(ruleForm.getCount()))
                .difficulty(ruleForm.getDifficulty())
                .build();
        ruleRepository.save(rule1);
        RsData.of("S-1", "규칙이 수정 되었습니다.", rule1);
    }

    public void setForm(Long ruleId, RuleForm ruleForm) {
        Rule rule = getRule(ruleId).getData();

        if (rule.getName() != null) {
            ruleForm.setName(rule.getName());
        } else {
            throw new NotFoundData("이름이 없음");
        }
        String strXp = String.valueOf(rule.getXp());
        String strCount = String.valueOf(rule.getCount());
        ruleForm.setXp(strXp);
        ruleForm.setCount(strCount);
        if (rule.getAbout() != null) {
            ruleForm.setAbout(rule.getAbout());
        } else {
            throw new NotFoundData("소개 없음");
        }
        if (rule.getProvider() != null) {
            ruleForm.setProvider(rule.getProvider());
        } else {
            throw new NotFoundData("OJ 입력없음");
        }
        if (rule.getDifficulty() != null) {
            ruleForm.setDifficulty(rule.getDifficulty());
        } else {
            throw new NotFoundData("난이도 없음");
        }

    }

    /**
     * 조회
     */

    public RsData<Rule> getRule(Long id) {
        Optional<Rule> rs = ruleRepository.findById(id);
        return rs.map(rule -> RsData.of("S-1", "Rule 조회 성공", rule))
                .orElseGet(() -> RsData.of("F-1", "Rule 조회 실패"));
    }

    public RsData<Rule> getRule(String name) {
        Optional<Rule> rs = ruleRepository.findByName(name);
        return rs.map(rule -> RsData.of("S-1", "Rule 조회 성공" ,rule))
                .orElseGet(() -> RsData.of("F-1", "Rule 조회 실패"));
    }

    public List<Rule> getRuleList() {
        return ruleRepository.findAll();
    }

    public Page<Rule> getList(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return ruleRepository.findAll(pageable);
    }

    public Page<Rule> getList(String keyword, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return ruleRepository.findByNameContaining(keyword, pageable);
    }

    /**
     * 삭제
     */
    @Transactional
    public void delete(Rule rule) {
        this.ruleRepository.delete(rule);

        RsData.of("S-1", "규칙이 삭제 되었습니다.");
    }

}
