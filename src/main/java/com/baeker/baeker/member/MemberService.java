package com.baeker.baeker.member;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.embed.BaekJoonDto;
import com.baeker.baeker.member.form.MemberInfoForm;
import com.baeker.baeker.member.form.MemberJoinForm;
import com.baeker.baeker.member.form.MemberModifyForm;
import com.baeker.baeker.member.snapshot.MemberSnapshot;
import com.baeker.baeker.member.snapshot.MemberSnapshotRepository;
import com.baeker.baeker.myStudy.MyStudy;
import com.baeker.baeker.myStudy.MyStudyQueryRepository;
import com.baeker.baeker.study.Study;
import com.baeker.baeker.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MyStudyQueryRepository myStudyQRepository;
    private final PasswordEncoder encoder;
    private final StudyService studyService;
    private final MemberSnapshotRepository memberSnapshotRepository;

    String url = "";

    /**
     ** 조회 관련 method **
     * find by username
     * find by id
     * find by 백준 name
     * find all
     * find All + paging
     * Study 가입 여부 조회
     */
    //-- find by username --//
    public Optional<Member> getMember(String username) {
        return memberRepository.findByUsername(username);
    }

    //-- find by id --//
    public RsData<Member> getMember(Long id) {
        Optional<Member> byId = memberRepository.findById(id);

        if (byId.isPresent())
            return RsData.successOf(byId.get());

        return RsData.of("F-1", "존재하지 않는 id 입니다.");
    }

    //-- find by 백준 name --//
    public RsData<Member> getByBaekJoonName(String baekJoonName) {
        Optional<Member> byId = memberRepository.findByBaekJoonName(baekJoonName);

        if (byId.isPresent())
            return RsData.successOf(byId.get());

        return RsData.of("F-1", "존재하지 않는 id 입니다.");
    }

    //-- find all --//
    public RsData<List<Member>> getAll() {
        List<Member> memberList = memberRepository.findAll();

        if (memberList.size() == 0)
            return RsData.failOf(memberList);

        return RsData.of("S-1", "Size = {}" + memberList.size(), memberList);
    }

    //-- find all + paging --//
    public Page<Member> getAll(int page) {
        ArrayList<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.asc("createDate"));

        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
        return memberRepository.findAll(pageable);
    }

    /**
     ** 생성과 로그인 관련 method **
     * Security Join      - admin, init data 용
     * Scocil join, login - 일반 회원용
     * 회원가입 처리 method
     * 백준 id 연동
     */

    //-- Security Join ( password 검증 ) --//
    @Transactional
    public RsData<Member> join(MemberJoinForm form) {

        if (!form.getPassword().equals(form.getPassword2()))
            return RsData.of("F-1", "비밀번호가 일치하지 않습니다.");

        return join("Baeker", form.getUsername(), form.getNickName(), form.getAbout(), form.getPassword(), form.getProfileImg(), "", ""); // !!! 일반 로그인 이메일 항목 추가해야 함!!!
    }

    //-- Social Join, Login --//
    @Transactional
    public RsData<Member> whenSocialLogin(String provider, String username, String name, String email, String token, String profileImg) {
        Optional<Member> opMember = getMember(username);

        if (opMember.isPresent())
            return RsData.of("S-2", "로그인 되었습니다.", opMember.get());

        return join(provider, username, name, "", "", profileImg, email, token);
    }


    //-- Join : Social + Security 실질적인 처리 --//
    private RsData<Member> join(String provider, String username, String name, String about, String password, String profileImg, String email, String token) {

        if (this.getMember(username).isPresent()) {
            return RsData.of("F-1", "해당 아이디(%s)는 이미 사용중입니다.".formatted(username));
        }

        if (StringUtils.hasText(password)) {
            password = encoder.encode(password);
        }

        Member member = Member.createMember(provider, username, name, about, password, profileImg, email, token);
        memberRepository.save(member);
        return RsData.of("S-1", "회원가입이 완료되었습니다. \n로그인 해주세요.", member);
    }

    //-- 백준 id 연동 --//
    @Transactional
    public RsData<Member> connectBaekJoon(Member member, String baekJoonName) {

        Member connectBaekJoon = member.connectBaekJoon(baekJoonName);
        Member saveMember = memberRepository.save(connectBaekJoon);
        this.saveSnapshot(member);

        return RsData.of("S-1", "백준 연동에 성공했습니다.", saveMember);
    }

    /**
     * 수정 관련 method
     * nick name, about, img 수정
     * Study leader 의 nickname 변경시 study leader 변경
     */

    //-- Modify Form 으로 nick name, about, img 수정 --//
    @Transactional
    public RsData<Member> modify(Member member, MemberModifyForm form) {

        return modify(member, form.getNickName(), form.getAbout(), form.getProfileImg());
    }

    //-- Info Form 으로 nick name, about, img 수정 --//
    @Transactional
    public RsData<Member> modify(Member member, MemberInfoForm form) {

        return modify(member, form.getNickName(), form.getAbout(), form.getProfileImg());
    }


    //-- 프로필 닉네임, 소개, 이미지 변경 실질적인 처리 --//
    private RsData<Member> modify(Member member, String nickName, String about, String profileImg) {

        if (nickName.contains("운영자") && !member.getUsername().equals("admin"))
            return RsData.of("F-1", nickName + "(은)는 사용할 수 없는 이름입니다.");

        List<MyStudy> myStudies = myStudyQRepository.findLeader(member);

        Member modifyMember = member.modifyMember(nickName, about, profileImg);
        Member saveMember = memberRepository.save(modifyMember);

        return this.modifyLeader(saveMember, myStudies);
    }


    //-- 스터디 리더의 이름이 변경될경우 스터디 리더 변경 --//
    private RsData<Member> modifyLeader(Member member, List<MyStudy> myStudies) {

        if (myStudies.size() == 0)
            return RsData.of("S-1", "수정이 완료되었습니다.", member);

        for (MyStudy myStudy : myStudies) {
            RsData<Study> studyRs = studyService.modifyLeader(member, myStudy.getStudy().getId());

            if (studyRs.isFail())
                return RsData.of("F-2", studyRs.getMsg());
        }

        return RsData.of("S-1", "수정이 완료되었습니다.", member);
    }

    /**
     * Event 와 Snapshot 관련 method
     * Snapshot 저장
     * Snapshot 삭제
     * 백준 solved count update 이벤트 처리
     */

    //-- 백준 최초 연동시 더미 스냅샷 7개 생성 --//
    private void saveSnapshot(Member member) {
        for (int i = 6; i >= 0; i--) {
            BaekJoonDto dummy = new BaekJoonDto();
            String dayOfWeek = LocalDateTime.now().minusDays(i).getDayOfWeek().toString();

            MemberSnapshot snapshot = MemberSnapshot.create(member, dummy, dayOfWeek.substring(0, 3));
            memberSnapshotRepository.save(snapshot);
        }
    }

    //-- 스냅샷 저장 --//
    private void saveSnapshot(Long id, BaekJoonDto dto) {

        String today = LocalDate.now().getDayOfWeek().toString().substring(0, 3);
        Member member = getMember(id).getData();
        MemberSnapshot snapshot = member.getSnapshotList().get(0);
        String createDay = snapshot.getDayOfWeek();

        if (createDay.equals(today)) snapshot = snapshot.update(dto);

        else {
            snapshot = MemberSnapshot.create(member, dto, today);
            this.deleteSnapshot(member);
        }
        memberSnapshotRepository.save(snapshot);
    }

    //-- 스냅샷 삭제 --//
    private void deleteSnapshot(Member member) {
        List<MemberSnapshot> snapshotList = member.getSnapshotList();
        MemberSnapshot snapshot = snapshotList.get(snapshotList.size() - 1);

        snapshotList.remove(snapshot);
        memberSnapshotRepository.delete(snapshot);
    }

    //-- 백준 Solved Count 이벤트 처리 --//
    public void whenBaekJoonEventType(Member member, BaekJoonDto dto) {

        // bug 지점
        this.saveSnapshot(member.getId(), dto);

        Member updateMember = member.updateBaeJoon(dto);
        memberRepository.save(updateMember);
    }

    /**
     * 기타 business 관련 method
     * 1 - 999 까지 랜덤 숫자 생성 - 랜덤 프로필 이미지 목록 생성용
     * 6 자리 랜덤 숫자 생성       - 백준 id 연동시 인증 코드 생성용
     */

    //-- 1~999 랜덤숫자 생성 --//
    public List<Integer> random() {

        List<Integer> random = new ArrayList<>();

        for (int i = 0; i < 10; i++)
            random.add((int) (Math.random() * 999) + 1);

        return random;
    }

    //-- 랜덤숫자 6자리 생성 --//
    public int verifyCode() {
        return (int) (Math.random() * 1000000);
    }

    /**
     * Init Data 관련 method
     * member 와 7 일간의 빈 snapshot 생성
     * snpshot 생성
     */

    //-- init db 용 create method --//
    @Transactional
    public Member initDbMemberCreate(String provider, String username, String name, String about, String password, String profileImg, String baekJoonName, BaekJoonDto dto) {

        if (StringUtils.hasText(password)) {
            password = encoder.encode(password);
        }

        Member member = Member.initMemberCreate(provider, username, name, about, password, profileImg, baekJoonName, dto);
        Member saveMember = memberRepository.save(member);

        if (saveMember.getBaekJoonName() != null)
            this.saveSnapshot(member);
        return saveMember;
    }

    //-- init db 용 create member snapshot --//
    @Transactional
    public void initDbSnapshotCreate(Member member, BaekJoonDto dto, String dayOfWeek) {

        MemberSnapshot snapshot = MemberSnapshot.initDbCreate(member, dto, dayOfWeek.substring(0, 3));
        memberSnapshotRepository.save(snapshot);
        this.deleteSnapshot(member);
    }
}