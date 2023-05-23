package com.baeker.baeker.solvedApi;

import com.baeker.baeker.base.event.BaekJoonEvent;
import com.baeker.baeker.base.event.StudyEvent;
import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.MemberService;
import com.baeker.baeker.member.embed.BaekJoonDto;
import com.baeker.baeker.studyRule.StudyRule;
import com.baeker.baeker.studyRule.StudyRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

import java.time.LocalDate;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class ApiScheduler {

    private final SolvedApiService solvedApiService;

    private final MemberService memberService;

    private final ApplicationEventPublisher publisher;
    private final StudyRuleService studyRuleService;

    /**
     * 티어 별 check
     */


//    @Scheduled(cron = "${scheduler.cron.value}")
//    public void checkStudyRule() throws IOException, ParseException {
//        log.info("스케줄러 실행");
//        RsData<List<Member>> memberList = memberService.getAll();
//        List<Member> members = new ArrayList<>();
//        for (Member member : memberList.getData()) {
//            Long memberId = member.getId();
//            try {
//                Thread.sleep(500);
//                solvedApiService.getSolvedCount(memberId);
//            } catch (HttpClientErrorException | InterruptedException e) {
//                members.add(member);
//                System.out.println("============================================");
//            }
//        }
//        if (!members.isEmpty()) {
//            for (Member member : members) {
//                solvedApiService.getSolvedCount(member.getId());
//            }
//        }
//    }

    @Scheduled(cron = "${scheduler.cron.member}")
    public void checkMember() throws IOException, ParseException {
        log.info("스케줄러 실행 day of week = {}", LocalDate.now().getDayOfWeek());
        RsData<List<Member>> memberList = memberService.getAll();
        for (Member member : memberList.getData()) {
            try {
                Long memberId = member.getId();
                int Bronze = solvedApiService.getSolvedCount(memberId, 1, 6) - member.getBronze();
                Thread.sleep(1000);

                int Silver = solvedApiService.getSolvedCount(memberId, 6, 11) - member.getSliver();
                Thread.sleep(1000);

                int Gold = solvedApiService.getSolvedCount(memberId, 11, 16) - member.getGold();
                Thread.sleep(1000);

                int Platinum = solvedApiService.getSolvedCount(memberId, 16, 21) - member.getPlatinum();
                Thread.sleep(1000);

                int Diamond = solvedApiService.getSolvedCount(memberId, 21, 26) - member.getDiamond();
                Thread.sleep(1000);

                int Ruby = solvedApiService.getSolvedCount(memberId, 26, 31) - member.getRuby();
                Thread.sleep(1000);

                BaekJoonDto dto = new BaekJoonDto(Bronze, Silver, Gold, Platinum, Diamond, Ruby);
                publisher.publishEvent(new BaekJoonEvent(this, member, dto));

            } catch (HttpClientErrorException | InterruptedException e) {
                log.info("###############" + e + "###############");
            }
        }
        log.info("스케줄러 {} 요일 업데이트 완료", LocalDate.now().getDayOfWeek());

    }

    @Scheduled(cron = "${scheduler.cron.study}")
    public void checkStudy() {
        log.info("스터디 스케줄러 ");
        List<StudyRule> studyRules = studyRuleService.getAll();
        for (StudyRule studyRule : studyRules) {
            Long studyRuleId = studyRule.getId();

            publisher.publishEvent(new StudyEvent(this, studyRuleId));
        }
    }

}