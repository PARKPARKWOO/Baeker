package com.baeker.baeker.myStudy;

import com.baeker.baeker.base.request.Rq;
import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.MemberService;
import com.baeker.baeker.member.embed.BaekJoon;
import com.baeker.baeker.myStudy.form.MyStudyInviteForm;
import com.baeker.baeker.myStudy.form.MyStudyJoinForm;
import com.baeker.baeker.study.Study;
import com.baeker.baeker.study.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/my_study")
@RequiredArgsConstructor
public class MyStudyController {

    private final MyStudyService myStudyService;
    private final MemberService memberService;
    private final StudyService studyService;
    private final Rq rq;

    //-- 가입 신청하기 --//
    @PostMapping("join/{id}")
    @PreAuthorize("isAuthenticated()")
    public String join(
            @PathVariable Long id,
            MyStudyJoinForm form
    ) {
        log.info("스터디 가입 요청 확인 study id = {}", id);

        Member member = rq.getMember();
        RsData<Study> studyRs = studyService.getStudy(id);

        if (studyRs.isFail()) {
            log.info("study 조회 실패 error = {}", studyRs.getMsg());
            return rq.historyBack(studyRs.getMsg());
        }

        RsData<MyStudy> joinRs = myStudyService.join(member, studyRs.getData(), form.getMsg());

        if (joinRs.isFail()) {
            log.info("스터디 가입 요청 실패 error = {}", joinRs.getMsg());
            return rq.historyBack(joinRs.getMsg());
        }

        log.info("스터디 가입 성공 가입 메시지 = {}", joinRs.getData().getMsg());
        return rq.redirectWithMsg("/member/profile/join", joinRs.getMsg());
    }

    //-- 스터디로 초대하기 --//
    @PostMapping("invite/{id}")
    @PreAuthorize("isAuthenticated()")
    public String invite(
            @PathVariable Long id,
            MyStudyInviteForm form
    ) {
        Member inviter = rq.getMember();
        log.info("스터디 초대 요청 확인 inviter id = {}", inviter.getId());

        RsData<Member> inviteeRs = memberService.getMember(id);
        if (inviteeRs.isFail()) {
            log.info("invitee 조회 실패 error = {}", inviteeRs.getMsg());
            return rq.historyBack(inviteeRs.getMsg());
        }

        RsData<Study> studyRs = studyService.getStudy(form.getId());
        if (studyRs.isFail()) {
            log.info("study 조회 실패 error = {}", studyRs.getMsg());
            return rq.historyBack(studyRs.getMsg());
        }

        RsData<MyStudy> inviteRs
                = myStudyService.invite(inviter, inviteeRs.getData(), studyRs.getData(), form.getMsg());

        if (inviteeRs.isFail()) {
            log.info("초대 실패 error = {}", inviteRs.getMsg());
            return rq.historyBack(inviteRs.getMsg());
        }

        log.info("초대 성공 초대 메시지 = {}", form.getMsg());
        return rq.redirectWithMsg("/study/detail/req/" + studyRs.getData().getId(), inviteRs.getMsg());
    }

    //-- 초대, 가입 요청 매시지 수정 --//
    @PostMapping("/modify/msg/{id}")
    @PreAuthorize("isAuthenticated()")
    public String modifyMsg(
            @PathVariable Long id,
            @RequestParam String msg
    ) {
        log.info("메시지 수정 요청 확인 my study id = {}", id);
        RsData<MyStudy> myStudyRs = myStudyService.getMyStudy(id);

        if (myStudyRs.isFail()) {
            log.info("스터디 조회 실패 error = {}", myStudyRs.getMsg());
            return rq.historyBack(myStudyRs.getMsg());
        }

        MyStudy myStudy = myStudyRs.getData();
        MyStudy modifyMyStudy = myStudyService.modifyMsg(myStudy, msg);
        return rq.historyBack("메시지 변경 완료");
    }

    //-- 초대 승인 --//
    @PostMapping("/me/accept/{id}")
    @PreAuthorize("isAuthenticated()")
    public String acceptInvite(
            @PathVariable Long id
    ) {
        log.info("스터디 맴버로 승인 요청 확인 my study id = {}", id);
        RsData<MyStudy> myStudyRs = myStudyService.getMyStudy(id);

        if (myStudyRs.isFail()) {
            log.info("my study 조회 실패 error = {}", myStudyRs.getMsg());
            return rq.historyBack(myStudyRs.getMsg());
        }

        MyStudy myStudy = myStudyRs.getData();
        Member member = rq.getMember();

        if (!myStudy.getMember().getNickName().equals(member.getNickName())) {
            log.info("승인 요청자가 본인이 아님 my nickname = {}, 요청자 name = {}", member.getNickName(), myStudy.getMember().getNickName());
            return rq.historyBack("권한이 없습니다.");
        }

        RsData<BaekJoon> acceptRs = myStudyService.accept(myStudy);

        if (acceptRs.isFail()) {
            log.info("승인 실패 error = {}", acceptRs.getMsg());
            return rq.historyBack(acceptRs.getMsg());
        }

        log.info("정식 스터디 원으로 승인 완료");
        return rq.redirectWithMsg("/member/profile/study#list", acceptRs.getMsg());
    }

    //-- 가입 승인 --//
    @PostMapping("/study/accept/{id}")
    @PreAuthorize("isAuthenticated()")
    public String acceptJoin(
            @PathVariable Long id
    ) {
        log.info("스터디 맴버로 승인 요청 확인 my study id = {}", id);
        RsData<MyStudy> myStudyRs = myStudyService.getMyStudy(id);

        if (myStudyRs.isFail()) {
            log.info("my study 조회 실패 error = {}", myStudyRs.getMsg());
            return rq.historyBack(myStudyRs.getMsg());
        }

        MyStudy myStudy = myStudyRs.getData();
        Member member = rq.getMember();

        if (!myStudy.getStudy().getLeader().equals(member.getNickName())) {
            log.info("승인 요청자가 스터디 리더가 아님 nick name = {}, leader = {}", member.getNickName(), myStudy.getStudy().getLeader());
            return rq.historyBack("권한이 없습니다.");
        }

        RsData<BaekJoon> acceptRs = myStudyService.accept(myStudy);

        if (acceptRs.isFail()) {
            log.info("승인 실패 error = {}", acceptRs.getMsg());
            return rq.historyBack(acceptRs.getMsg());
        }

        log.info("정식 스터디 원으로 승인 완료");
        return rq.redirectWithMsg("/study/detail/member/" + myStudy.getStudy().getId(), acceptRs.getMsg());
    }

    //-- 나의 초대, 가입 요청 삭제 --//
    @DeleteMapping("/me/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String myReject(
            @PathVariable Long id
    ) {
        log.info("초대 거절 요청 확인 my study id = {}", id);
        Member member = rq.getMember();
        RsData<MyStudy> myStudyRs = myStudyService.getMyStudy(id);

        if (myStudyRs.isFail()) {
            log.info("my study 조회 실패 error = {}", myStudyRs.getMsg());
            return rq.historyBack(myStudyRs.getMsg());
        }

        MyStudy myStudy = myStudyRs.getData();
        if (!member.getNickName().equals(myStudy.getMember().getNickName())) {
            log.info("거절 요청자가 본인이 아님 my nickname = {}, 요청자 name = {}", member.getNickName(), myStudy.getMember().getNickName());
            return rq.historyBack("권한이 없습니다.");
        }

        RsData rsData = myStudyService.delete(myStudy);
        log.info("초대 거절 완료");
        return rq.redirectWithMsg("/member/profile/join", rsData.getMsg());
    }

    //-- 스터디 초대, 가입 요청 삭제 --//
    @DeleteMapping("/study/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String studyReject(
            @PathVariable Long id
    ) {
        log.info("초대 거절 요청 확인 my study id = {}", id);
        Member member = rq.getMember();
        RsData<MyStudy> myStudyRs = myStudyService.getMyStudy(id);

        if (myStudyRs.isFail()) {
            log.info("my study 조회 실패 error = {}", myStudyRs.getMsg());
            return rq.historyBack(myStudyRs.getMsg());
        }

        MyStudy myStudy = myStudyRs.getData();
        if (!member.getNickName().equals(myStudy.getStudy().getLeader())) {
            log.info("거절 요청자가 스터디 리더가 아님 nick name = {}, leader = {}", member.getNickName(), myStudy.getStudy().getLeader());
            return rq.historyBack("권한이 없습니다.");
        }

        RsData rsData = myStudyService.delete(myStudy);
        log.info("초대 거절 완료");
        return rq.redirectWithMsg("/member/profile/join", rsData.getMsg());
    }
}
