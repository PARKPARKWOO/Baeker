package com.baeker.baeker.rule;

import com.baeker.baeker.base.request.RsData;
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
                .difficulty(ruleForm.getDifficulty())
                .build();
        ruleRepository.save(rule1);
        RsData.of("S-1", "규칙이 수정 되었습니다.", rule1);
    }
    /**
     * PATCH
     * @param id
     * @param updates
     * @return
     */
    public RuleForm updateRule(Long id, Map<String, String> updates) {
        String name = updates.get("name");
        String about = updates.get("about");
        String strXp = updates.get("xp");
        String strCount = updates.get("count");
        String provider = updates.get("provider");
        String difficulty = updates.get("difficulty");

        RuleForm ruleForm = new RuleForm();
        setForm(id, ruleForm);
        if (name != null) {
            ruleForm.setName(name);
        }
        if (about != null) {
            ruleForm.setAbout(about);
        }
        if (strXp != null) {
            try {
                Integer xp = Integer.parseInt(strXp);
                ruleForm.setXp(xp.toString());
            } catch (NumberFormatException e) {
                throw new NumberInputException("xp는 숫자로 입력해주세요");
            }
        }
        if (strCount != null) {
            try {
                Integer count = Integer.parseInt(strCount);
                ruleForm.setCount(count.toString());
            } catch (NumberFormatException e) {
                throw new NumberInputException("Count는 숫자로 입력해주세요");
            }
        }
        if (provider != null) {
            ruleForm.setProvider(provider);
        }
        if (difficulty != null) {
            ruleForm.setDifficulty(difficulty);
        }
        return ruleForm;
    }

    public void setForm(Long ruleId, RuleForm ruleForm) {
        Rule rule = getRule(ruleId).getData();
        Optional<String> name = Optional.of(rule.getName());
        Optional<String> about = Optional.of(rule.getAbout());
        Optional<Integer> xp = Optional.of(rule.getXp());
        Optional<Integer> count = Optional.of(rule.getCount());
        Optional<String > provider = Optional.of(rule.getProvider());
        Optional<String> difficulty = Optional.of(rule.getDifficulty());
        if (name.isPresent()) {
            ruleForm.setName(name.get());
        }
        if (about.isPresent()) {
            ruleForm.setAbout(about.get());
        }
        if (xp.isPresent()) {
            ruleForm.setXp(xp.get().toString());
        }
        if (count.isPresent()) {
            ruleForm.setXp(count.get().toString());
        }
        if (provider.isPresent()) {
            ruleForm.setCount(provider.get());
        }
        if (difficulty.isPresent()) {
            ruleForm.setDifficulty(difficulty.get());
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
