package com.baeker.baeker.study;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.MemberService;
import com.baeker.baeker.myStudy.MyStudy;
import com.baeker.baeker.myStudy.MyStudyRepository;
import com.baeker.baeker.myStudy.MyStudyService;
import com.baeker.baeker.study.form.StudyCreateForm;
import com.baeker.baeker.study.form.StudyModifyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final MyStudyRepository myStudyRepository;


    //-- create --//
    @Transactional
    public RsData<Study> create(StudyCreateForm form, Member member) {

        MyStudy myStudy = Study.createStudy(form.getName(), form.getAbout(), form.getCapacity(), member);

        myStudyRepository.save(myStudy);
        Study saveStudy = studyRepository.save(myStudy.getStudy());
        return RsData.of("S-1", "새로운 스터디가 개설되었습니다!", saveStudy);
    }


    //-- find by id --//
    public RsData<Study> getStudy(Long id) {
        Optional<Study> byId = studyRepository.findById(id);

        if (byId.isPresent())
            return RsData.successOf(byId.get());

        return RsData.of("F-1", "존재 하지않는 id");
    }

    //-- find all member --//
    public RsData<List<Member>> getAllMember(Long id) {
        List<Member> members = new ArrayList<>();
        RsData<Study> studyRs = this.getStudy(id);

        if (studyRs.isFail()) return RsData.of("F-1", studyRs.getMsg());
        List<MyStudy> myStudies = studyRs.getData().getMyStudies();

        for (MyStudy myStudy : myStudies) {
            members.add(myStudy.getMember());
        }
        return RsData.successOf(members);
    }

    //-- find all --//
    public List<Study> getAll() {
        return studyRepository.findAll();
    }


    //-- 이름, 소개, 최대 인원 변경 --//
    @Transactional
    public RsData<Study> modify(StudyModifyForm form, Long id) {
        RsData<Study> studyRs = this.getStudy(id);

        if (studyRs.isFail()) return studyRs;
        Study study = studyRs.getData();

        Study modifyStudy = study.modifyStudy(form.getName(), form.getAbout(), form.getCapacity());
        studyRepository.save(modifyStudy);
        return RsData.of("S-1", "수정이 완료되었습니다.", modifyStudy);
    }

    //-- 리더 변경 --//
    @Transactional
    public RsData<Study> modifyLeader(Member member, Long id) {
        RsData<Study> studyRs = this.getStudy(id);

        if (studyRs.isFail()) return studyRs;
        Study study = studyRs.getData();

        Study modifyLeader = study.modifyLeader(member.getName());
        studyRepository.save(modifyLeader);
        return RsData.of("S-1", "리더가 변경되었습니다.", modifyLeader);
    }

    //-- 경험치 상승 --//
    @Transactional
    public RsData<Study> xpUp(Integer xp, Long id) {
        RsData<Study> studyRs = this.getStudy(id);

        if (studyRs.isFail()) return studyRs;
        Study study = studyRs.getData();

        study.xpUp(xp);
        return RsData.of("S-1", "경험치가 상승했습니다.", study);
    }


    //-- 스터디 삭제 --//
    @Transactional
    public RsData<Study> delete(Long id) {
        RsData<Study> studyRs = this.getStudy(id);

        if (studyRs.isFail())
            return studyRs;

        studyRepository.delete(studyRs.getData());
        return RsData.of("S-1", "스터디가 삭제되었습니다.");
    }

    //-- 스터디원 id 로 찾기 --//
    public RsData<Member> getMember(Long memberId,Long studyId) {
        RsData<List<Member>> studyMemberRs = this.getAllMember(studyId);

        if (studyMemberRs.isFail())
            return RsData.of("F-1", studyMemberRs.getMsg());

        List<Member> members = studyMemberRs.getData();

        for (Member member : members) {
            if (member.getId() == memberId)
                return RsData.successOf(member);
        }

        return RsData.of("F-2", "스터디에 존재하지 않는 회원입니다.");
    }
}