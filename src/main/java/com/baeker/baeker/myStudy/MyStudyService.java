package com.baeker.baeker.myStudy;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.study.Study;
import com.baeker.baeker.study.StudyService;
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
    private final MyStudyQueryRepository myStudyQRepository;

    /**
     * * 생성 관련 method **
     * Study 개설시 개설자 등록 작업
     * 가입 신청
     * 스터디에 초대
     * 스터디 중복 가입여부 확인
     */

    //-- Study 개설시 개설자 등록 작업 --//
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
    public RsData<MyStudy> invite(Member inviter, Member invitee, Study study, String msg) {

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

    //-- 스터디 중복 가입 확인 --//
    private static RsData<MyStudy> duplicationCheck(Member member, Study study) {
        List<MyStudy> myStudies = member.getMyStudies();

        for (MyStudy myStudy : myStudies) {
            if (myStudy.getStudy().equals(study))
                return RsData.of("F-1", "이미 가입한 스터디입니다.");
        }
        return RsData.of("S-1", "성공");
    }


    /**
     * * 수정과 삭제 관련 method **
     * 가입과 초대 승인
     * 가입과 초대 메시지 변경
     * 가입 요청과 초대 삭제
     */

    //-- 가입, 초대신청 승인 --//
    @Transactional
    public RsData<MyStudy> accept(MyStudy myStudy) {

        if (myStudy.getStudy().equals(StudyStatus.MEMBER))
            return RsData.of("F-1", "이미 정식 스터디 맴버입니다.");

        Study study = myStudy.getStudy();
        if (study.getMyStudies().size() == study.getCapacity())
            return RsData.of("F-2", "이미 최대 인원에 도달했습니다.");

        myStudy.accept();


        return RsData.of("S-1", "정식 회원으로 가입이 완료되었습니다.");
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


    /**
     * * 조회 관련 method **
     * find by id
     * find by member & study
     * find Status == member by member
     * find Status == member by study
     * find Status != member by member
     * find Status != member by study
     * find by Member when leader
     */

    //-- find by id --//
    public RsData<MyStudy> getMyStudy(Long id) {
        Optional<MyStudy> byId = myStudyRepository.findById(id);

        if (byId.isPresent())
            return RsData.successOf(byId.get());

        return RsData.of("F-1", "존재하지 않는 id 입니다.");
    }

    //-- find by member, study --//
    public RsData<MyStudy> getMyStudy(Member member, Study study) {
        List<MyStudy> myStudies = myStudyQRepository.getMyStudy(member, study);

        if (myStudies.size() == 0)
            return RsData.of("F-1", "가입하지 않은 스터디 입니다.");

        return RsData.successOf(myStudies.get(0));
    }

    //-- member 가 정회원인 my study 조회 --//
    public List<MyStudy> statusMember(Member member) {
        return myStudyQRepository.statusMember(member);
    }

    //-- 스터디 가입 승인이 완료된 my study 조회 --//
    public List<MyStudy> statusMember(Study study) {
        return myStudyQRepository.statusMember(study);
    }


    //-- member 등급이 아닌 my study 조회 by member --//
    public List<MyStudy> statusNotMember(Member member) {
        return myStudyQRepository.statusNotMember(member);
    }

    //-- member 등급이 아닌 my study 조회 by study --//
    public List<MyStudy> statusNotMember(Study study) {
        return myStudyQRepository.statusNotMember(study);
    }

    //-- member 가 리더인 my study 조회 --//
    public RsData<List<MyStudy>> getMyStudyOnlyLeader(Member member) {
        List<MyStudy> leader = myStudyQRepository.findLeader(member);

        if (leader.size() == 0)
            return RsData.of("F-1", "내가 리더인 스터디가 없습니다.", leader);

        return RsData.successOf(leader);
    }
}

