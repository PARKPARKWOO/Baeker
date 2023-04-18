package com.baeker.baeker.myStudy;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.study.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyStudyService {

    private final MyStudyRepository myStudyRepository;

    //-- 스터디 가입 신청 --//
    @Transactional
    public RsData<MyStudy> join(Member member, Study study) {

        // 가입한 사람이 이미 스터디 맴버인지 검증
        RsData<MyStudy> isDuplicate = duplicationCheck(member, study);
        if (isDuplicate.isFail()) return isDuplicate;

        MyStudy myStudy = MyStudy.joinStudy(member, study);
        MyStudy save = myStudyRepository.save(myStudy);
        return RsData.of("S-1", study.getName() + "에 가입신청이 완료되었습니다.", save);
    }

    //-- 맴버 스터디에 초대 --//
    @Transactional
    public RsData<MyStudy> invite(Member inviter, Member invitee,  Study study) {

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

        MyStudy myStudy = MyStudy.inviteStudy(invitee, study);
        MyStudy save = myStudyRepository.save(myStudy);
        return RsData.of("S-1", inviter.getName() + "님을 스터디에 초대했습니다.", save);
    }

    //-- 가입, 초대신청 승인 --//
    @Transactional
    public RsData<MyStudy> accept(MyStudy myStudy) {

        if (myStudy.getStudy().equals(StudyStatus.MEMBER))
            return RsData.of("F-1", "이미 정식 스터디 맴버입니다.");

        myStudy.accept();
        return RsData.of("S-1", "정식 회원으로 가입이 완료되었습니다.", myStudy);
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
}