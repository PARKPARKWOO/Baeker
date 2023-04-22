package com.baeker.baeker.solvedApi;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.MemberService;
import com.baeker.baeker.member.embed.BaekJoon;
import com.baeker.baeker.study.StudyService;
import com.baeker.baeker.studyRule.StudyRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class ApiScheduler {

    private final SolvedApiManager solvedApiManager;

    private final SolvedApiService solvedApiService;

    private final StudyRuleService studyRuleService;

    private final StudyService studyService;

    private final MemberService memberService;

    /**
     * 티어 체크 후 Study
     */
//    @Scheduled(fixedRate = 1000)
//    public void tierSchedule() throws IOException, ParseException {
//        List<StudyRule> all = studyRuleService.getAll();
//        for (StudyRule studyRule : all) {
//            log.info("티어별 실행");
//            Integer solvedCount = solvedApiService.getSolvedCount(studyRule.getRule().getDifficulty().toUpperCase());
//            // solvedCount 값을 저장해 줄 곳 필요
////            studyRule.getGoal().getMyStudy().setGoal(solvedCount);
//            //
//            System.out.println(solvedCount);
//        }
//    }

    @Scheduled(fixedRate = 1000)
    public void checkStudyRule() throws IOException, ParseException {
        log.info("스케줄러 실행");
        RsData<List<Member>> memberList = memberService.getAll();
        for (Member member : memberList.getData()) {
            log.info("tier");
            Integer Bronze = solvedApiService.getSolvedCount(member, 1, 6);

            Integer Silver = solvedApiService.getSolvedCount(member, 6, 11);

            Integer Gold = solvedApiService.getSolvedCount(member, 11, 16);

            Integer Platinum = solvedApiService.getSolvedCount(member, 16, 21);

            Integer Diamond = solvedApiService.getSolvedCount(member, 21, 26);

            Integer Ruby = solvedApiService.getSolvedCount(member, 26, 31);

            BaekJoon baekJoon = BaekJoon.builder().bronze(Bronze).sliver(Silver).gold(Gold).platinum(Platinum)
                    .diamond(Diamond).ruby(Ruby).build();
            memberService.solve(member.getId(), baekJoon);
        }
    }
}
