package com.baeker.baeker.member;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.embed.BaekJoon;
import com.baeker.baeker.member.form.MemberJoinForm;
import com.baeker.baeker.myStudy.MyStudy;
import com.baeker.baeker.myStudy.MyStudyService;
import com.baeker.baeker.study.Study;
import com.baeker.baeker.study.StudyService;
import com.baeker.baeker.study.form.StudyCreateForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired private MemberService memberService;
    @Autowired private StudyService studyService;
    @Autowired private MyStudyService myStudyService;
    @Autowired private MemberRepository memberRepository;

    private Member create(String username, String name) {
        MemberJoinForm form = new MemberJoinForm(username, name, "", "1234", "1234", 1);
        return memberService.join(form).getData();
    }

    private Study createStudy(String name, Member member) {
        StudyCreateForm form = new StudyCreateForm(name, "about", 10);
        Study study = studyService.create(form, member).getData();
        myStudyService.create(member, study);
        return study;
    }

    @Test
    void 회원가입() {
        MemberJoinForm form = new MemberJoinForm("username", "name",  "","1234", "1234" ,1);
        RsData<Member> memberRs = memberService.join(form);
        Member member = memberRs.getData();

        Member findMember = memberService.getMember("username").get();

        assertThat(memberRs.getResultCode()).isEqualTo("S-1");
        assertThat(member).isSameAs(findMember);
        assertThat(member.getNickName()).isEqualTo(findMember.getNickName());
    }

    @Test
    void 해결한_문제_수_업데이트() {

        // member1 : study 생성
        Member member1 = create("user1", "member1");
        Study study = createStudy("study", member1);


        assertThat(member1.getBaekJoon()).isNull();
        assertThat(study.getBaekJoon()).isNull();

        BaekJoon baekJoon = BaekJoon.builder()
                .bronze(4)
                .sliver(2)
                .build();

        // member1 : 해결한 문제 백준 브론즈 4문제, 실버 2문제 추가
        RsData<BaekJoon> baekJoonRs = memberService.solve(member1.getId(), baekJoon);
        assertThat(baekJoonRs.getResultCode()).isEqualTo("S-1");

        // member1 이 속한 스터디까지 추가 완료
        assertThat(member1.getBaekJoon().getBronze()).isEqualTo(4);
        assertThat(member1.getBaekJoon().getSliver()).isEqualTo(2);
        assertThat(member1.getBaekJoon().totalSolved()).isEqualTo(6);
        assertThat(study.getBaekJoon().getSliver()).isEqualTo(2);


        // member2 : 백준 실버 8문제 해결
        Member member2 = create("user2", "member2");
        BaekJoon baekJoon2 = BaekJoon.builder()
                .sliver(8)
                .build();
        RsData<BaekJoon> solve = memberService.solve(member2.getId(), baekJoon2);
        assertThat(member2.getBaekJoon().getSliver()).isEqualTo(8);

        // member2 : study 가입
        MyStudy myStudy = myStudyService.join(member2, study, "hi").getData();
        RsData<BaekJoon> addedSolveRs = myStudyService.accept(myStudy);

        // 가입이 성공하면 member2 의 해결문제수가 study 에 자동 합산됨
        assertThat(addedSolveRs.getResultCode()).isEqualTo("S-1");
        assertThat(study.getBaekJoon().getSliver()).isEqualTo(10);

        // 반환값으로 추가된 값 반환
        assertThat(addedSolveRs.getData().getSliver()).isEqualTo(8);
    }

    @Test
    void 프로필_변경() {
        Member member = create("user1", "member1");

        assertThat(member.getNickName()).isEqualTo("member1");

        Optional<Member> findMember = memberRepository.findByNickName("member1");

        assertThat(findMember.isPresent()).isTrue();

        String nickName = findMember.get().getNickName();
        System.out.println(nickName);
    }

    @Test
    void 내가_리서인_스터디_조회() {
        Member leader = create("user1", "leader");
        Study myStudy1 = createStudy("my study1", leader);
        Study myStudy2 = createStudy("my study2", leader);

        Member member = create("user2", "member");
        Study study1 = createStudy("study1", member);
        Study study2 = createStudy("study2", member);

        List<Study> allStudies = studyService.getAll();
        List<MyStudy> allMyStudies = myStudyService.getAll();
        assertThat(allStudies.size()).isEqualTo(4);
        assertThat(allMyStudies.size()).isEqualTo(4);

        List<MyStudy> myStudyOnlyLeader = memberService.getMyStudyOnlyLeader(leader);
        assertThat(myStudyOnlyLeader.size()).isEqualTo(2);

        for (MyStudy myStudy : myStudyOnlyLeader) {
            String studyLeader = myStudy.getStudy().getLeader();
            assertThat(studyLeader).isEqualTo(leader.getNickName());
        }


    }
}