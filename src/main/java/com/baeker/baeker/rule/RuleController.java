package com.baeker.baeker.rule;

import com.baeker.baeker.base.request.Rq;
import com.baeker.baeker.base.request.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/rule")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    private final Rq rq;

    /**
     * 생성
     */

    @GetMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String showCreate(RuleForm ruleForm) {
        if (!rq.getMember().getUsername().equals("admin")) {
            return rq.redirectWithMsg("/rule/list", "관리자만 접근가능합니다");
        }
        return "rule/create";
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String create(@Valid RuleForm ruleForm, BindingResult bindingResult) {
        if (!rq.getMember().getUsername().equals("admin")) {
            return rq.redirectWithMsg("/rule/list", "관리자만 접근가능합니다");
        }
        RsData<Rule> rsData = ruleService.create(ruleForm);
        if (rsData.isFail() || bindingResult.hasErrors()) {
            return rq.historyBack(rsData);
        }
        return rq.redirectWithMsg("/rule/list", rsData.getMsg());
    }


    /**
     * 수정
     */
    @GetMapping("/modify/{id}")
    @PreAuthorize("isAuthenticated()")
    public String showModify(@PathVariable("id") Long id, RuleForm ruleForm) {
        if (!rq.getMember().getUsername().equals("admin")) {
            return rq.redirectWithMsg(String.format("/rule/detail/%d", id), "관리자만 접근가능합니다");
        }
        RsData<Rule> rsData = this.ruleService.getRule(id);
        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }
        Rule rule = rsData.getData();
        ruleService.setForm(rule.getId(), ruleForm);
        return "rule/create";
    }

    @PostMapping("/modify/{id}")
    @PreAuthorize("isAuthenticated()")
    public String modify(@PathVariable("id") Long id, @Valid RuleForm ruleForm, BindingResult bindingResult) {
        if (!rq.getMember().getUsername().equals("admin")) {
            return rq.redirectWithMsg(String.format("/rule/detail/%d", id), "관리자만 접근가능합니다");
        }
        RsData<Rule> rsData = ruleService.getRule(id);
        if (rsData.isFail() || bindingResult.hasErrors()) {
            return rq.historyBack("다시 확인해주세요");
        }
        ruleService.modify(rsData.getData().getId(), ruleForm);
        return rq.redirectWithMsg(String.format("/rule/detail/%s", id), "규칙이 수정되었습니다");
    }

    /**
     * 삭제
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable("id") Long id) {
        if (!rq.getMember().getUsername().equals("admin")) {
            return rq.redirectWithMsg(String.format("/rule/detail/%d", id), "관리자만 접근가능합니다");
        }
        RsData<Rule> rsData = ruleService.getRule(id);
        if (rsData.isSuccess()) {
            ruleService.delete(rsData.getData());
            return rq.redirectWithMsg("/rule/list", "삭제 되었습니다.");
        } else {
            return rq.historyBack(rsData.getMsg());
        }
    }

    /**
     * 조회/목록/검색
     */

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0")
                       int page, String keyword) {

        Page<Rule> paging;
        if (keyword != null) {
            paging = ruleService.getList(keyword, page);
        } else {
            paging = ruleService.getList(page);
        }

        model.addAttribute("paging", paging);
        return "rule/ruleList";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        RsData<Rule> rule = ruleService.getRule(id);
        if (rule.isFail()) {
            return rq.historyBack(rule);
        }
        model.addAttribute("rule", rule.getData());
        return "rule/detail";
    }

}
