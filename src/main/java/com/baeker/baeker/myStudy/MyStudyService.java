package com.baeker.baeker.myStudy;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.embed.BaekJoon;
import com.baeker.baeker.study.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyStudyService {

    private final MyStudyRepository myStudyRepository;

    //-- 새로운 스터디 개설 --//
    @Transactional
    public MyStudy create(Member member, Study study) {
        MyStudy newStudy = MyStudy.createNewStudy(member, study);
        MyStudy myStudy = myStudyRepository.save(newStudy);

        return myStudy;
    }

    //-- 스터디 가입 신청 --//
    @Transactional
    public RsData<MyStudy> join(Member member, Study study, String msg) {

        // 가입한 사람이 이미 스터디 맴버인지 검증
        RsData<MyStudy> isDuplicate = duplicationCheck(member, study);
        if (isDuplicate.isFail()) return isDuplicate;

        if (study.getMyStudies().size() == study.getCapacity())
            return RsData.of("F-2", "최대 인원에 도달하여 더 이상 인원을 받지 않습니다.");

        MyStudy myStudy = MyStudy.joinStudy(member, study, msg);
        MyStudy save = myStudyRepository.save(myStudy);
        return RsData.of("S-1", study.getName() + "에 가입신청이 완료되었습니다.", save);
    }

    //-- 맴버 스터디에 초대 --//
    @Transactional
    public RsData<MyStudy> invite(Member inviter, Member invitee,  Study study, String msg) {

        // 초대를 한 사람이 스터디원인지 검증
        RsData<MyStudy> isStudyMember = getMyStudy(inviter, study);
        if (isStudyMember.isFail())
            return isStudyMember;

        else if (!isStudyMember.getData().getStatus().equals(StudyStatus.MEMBER))
            return RsData.of("F-2", "초대 권한이 없습니다.");


        // 초대받은 사람이 스터디 맴버인지 검증
        RsData<MyStudy> isDuplicate = duplicationCheck(invitee, study);
        if (isDuplicate.isFail())
            return isDuplicate;

        if (study.getMyStudies().size() == study.getCapacity())
            return RsData.of("F-2", "최대 인원에 도달해 더이상 초대할 수 없습니다.");

        MyStudy myStudy = MyStudy.inviteStudy(invitee, study, msg);
        MyStudy save = myStudyRepository.save(myStudy);
        return RsData.of("S-1", inviter.getNickName() + "님을 스터디에 초대했습니다.", save);
    }

    //-- 가입, 초대신청 승인 --//
    @Transactional
    public RsData<BaekJoon> accept(MyStudy myStudy) {

        if (myStudy.getStudy().equals(StudyStatus.MEMBER))
            return RsData.of("F-1", "이미 정식 스터디 맴버입니다.");

        Study study = myStudy.getStudy();
        if (study.getMyStudies().size() == study.getCapacity())
            return RsData.of("F-2", "이미 최대 인원에 도달했습니다.");

        BaekJoon addedSolved = myStudy.accept();
        return RsData.of("S-1", "정식 회원으로 가입이 완료되었습니다.", addedSolved);
    }


    //-- find by id --//
    public RsData<MyStudy> getMyStudy(Long id) {
        Optional<MyStudy> byId = myStudyRepository.findById(id);

        if (byId.isPresent())
            return RsData.successOf(byId.get());

        return RsData.of("F-1", "존재하지 않는 id 입니다.");
    }

    //-- find by member, study --//
    public RsData<MyStudy> getMyStudy(Member member, Study study) {
        List<MyStudy> myStudies = member.getMyStudies();

        for (MyStudy myStudy : myStudies) {
            if (myStudy.getStudy().equals(study))
                return RsData.successOf(myStudy);
        }
        return RsData.of("F-1", "가입하지 않은 스터디 입니다.");
    }

    //-- 스터디 중복 가입 확인 --//
    private static RsData<MyStudy> duplicationCheck(Member member, Study study) {
        List<MyStudy> myStudies = member.getMyStudies();

        for (MyStudy myStudy : myStudies) {
            if (myStudy.getStudy().equals(study))
                return RsData.of("F-1", "이미 가입한 스터디입니다.");
        }
        return RsData.of("S-1", "성공");
    }

    //-- member 등급인 my study 만 조회 --//
    public List<MyStudy> statusMember(Member member) {
        List<MyStudy> myStudies = new ArrayList<>();
        List<MyStudy> myStudyList = member.getMyStudies();

        for (MyStudy myStudy : myStudyList)
            if (myStudy.getStatus().equals(StudyStatus.MEMBER))
                myStudies.add(myStudy);

        return myStudies;
    }

    public List<MyStudy> statusMember(Study study) {
        List<MyStudy> myStudies = new ArrayList<>();
        List<MyStudy> myStudyList = study.getMyStudies();

        for (MyStudy myStudy : myStudyList)
            if (myStudy.getStatus().equals(StudyStatus.MEMBER))
                myStudies.add(myStudy);

        return myStudies;
    }


    //-- member 등급이 아닌 my study 조회 --//
    public List<MyStudy> statusNotMember(Member member) {
        List<MyStudy> pending = new ArrayList<>();
        List<MyStudy> myStudies = member.getMyStudies();

        for (MyStudy myStudy : myStudies)
            if (!myStudy.getStatus().equals(StudyStatus.MEMBER))
                pending.add(myStudy);

        return pending;
    }

    public List<MyStudy> statusNotMember(Study study) {
        List<MyStudy> pending = new ArrayList<>();
        List<MyStudy> myStudies = study.getMyStudies();

        for (MyStudy myStudy : myStudies)
            if (!myStudy.getStatus().equals(StudyStatus.MEMBER))
                pending.add(myStudy);

        return pending;
    }

    //-- 초대, 가입 요청 메시지 변경 --//
    @Transactional
    public MyStudy modifyMsg(MyStudy myStudy, String msg) {
        MyStudy modify = myStudy.modifyMsg(msg);
        return myStudyRepository.save(modify);
    }

    //-- 초대, 가입 요청 삭제 --//
    @Transactional
    public RsData delete(MyStudy myStudy) {

        StudyStatus status = myStudy.getStatus();

        myStudyRepository.delete(myStudy);

        if (status == StudyStatus.INVITING)
            return RsData.of("S-1", "초대를 거절했습니다.");
        else
            return RsData.of("S-1", "가입 요청을 취소했습니다.");
    }
}
