package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ClubMemberAccountStatusException;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberAccountStatus;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import com.USWCicrcleLink.server.user.dto.ClubDTO;
import com.USWCicrcleLink.server.user.dto.ExistingMemberSignUpRequest;
import com.USWCicrcleLink.server.user.repository.ClubMemerAccountStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ClubMemberAccountStatusService {
    private final ClubMemerAccountStatusRepository clubMemerAccountStatusRepository;

    // clubMemberAccountStatus 객체 생성 메서드
    public void createAccountStatus(Club club, ClubMemberTemp clubMemberTemp){

        // ClubMemberAccountStatus 객체 생성
        ClubMemberAccountStatus status;
        try{
            status = ClubMemberAccountStatus.createClubMemberAccountStatus(club, clubMemberTemp);
            log.debug("clubMember_Account_Status 객체 생성 완료- Club ID: {}, 사용자 ID : {}", club.getClubId(), clubMemberTemp.getId());
        }catch (Exception e){
            log.error("clubMember_Account_Status 객체 생성 실패- Club ID: {},  사용자 ID : {}", club.getClubId(), clubMemberTemp.getId());
            throw new ClubMemberAccountStatusException(ExceptionType.CLUB_MEMBER_ACCOUNTSTATUS_CREATE_FAILED);
        }

        // 생성된 ClubMemberAccountStatus 저장
        try{
            log.debug("clubMember_Account_Status 객체 저장 완료- Club ID: {}, 사용자 ID : {}", club.getClubId(), clubMemberTemp.getId());
            clubMemerAccountStatusRepository.save(status);
        }catch (Exception e){
            log.error("clubMember_Account_Status 객체 저장 실패- Club ID: {}, 사용자 ID : {}", club.getClubId(), clubMemberTemp.getId());
            throw new ClubMemberAccountStatusException(ExceptionType.CLUB_MEMBER_ACCOUNTSTATUS_CREATE_FAILED);
        }
        log.debug("ClubMemberAccountStatus 저장 완료 - Club ID: {}, 사용자 ID : {}", club.getClubId(), clubMemberTemp.getId());
    }

    // 각 동아리에 대한 요청 전송이 제대로 되었는지 검증
    // clubMemberTemp-accountStatus 테이블이 제대로 생성되었는지 확인
    public void checkRequest(ExistingMemberSignUpRequest request, ClubMemberTemp clubMemberTemp){

        log.debug("가입신청 검증 시작");

        // 총 생성된 ClubMemberAccountStatus 개수 확인
        long savedCount = clubMemerAccountStatusRepository.countByClubMemberTempId(clubMemberTemp.getId());
        int expectedCount = request.getClubs().size();

        log.debug("개수 검증 결과 - 사용자 ID: {}, 저장된 개수: {}, 예상 개수: {}",
                clubMemberTemp.getId(), savedCount, expectedCount);

        if (savedCount == expectedCount) {
            log.debug("모든 동아리에게 요청 개수 일치 - 사용자 ID: {}", clubMemberTemp.getId());
        } else {
            log.error("요청 개수 검증 실패 - 사용자 ID: {}, 저장된 개수: {}, 예상 개수: {}",
                    clubMemberTemp.getId(), savedCount, expectedCount);
            throw new ClubMemberAccountStatusException(ExceptionType.CLUB_MEMBER_ACCOUNTSTATUS_COUNT_NOT_MATCH);
        }

        // 사용자가 선택한 동아리에 올바르게 전송 되었는지 확인
        // 올바른 값 -- request에 저장된 clubId를 set으로 저장
        Set<Long> expected_clubId = request.getClubs().stream()
                .map(ClubDTO::getClubId)
                .collect(Collectors.toSet());

        // 검증하고자 하는 값 -- accountStatus 테이블에 생성된 값
        Set<Long> saved_clubId = clubMemerAccountStatusRepository.findByClubMemberTempId(clubMemberTemp.getId())
                .stream()
                .map(accountStatus -> accountStatus.getClub().getClubId())
                .collect(Collectors.toSet());

        // 두 set에 들어있는 clubId가 모두 일치하는지 확인하기
        if(expected_clubId.equals(saved_clubId)){
            log.debug("사용자가 요청한 동아리Id와 저장된 동아리Id 값이 모두 일치합니다");
        }
        else{
            log.error("사용자가 요청한 동아리Id와 저장된 동아리Id 값이 일치하지않습니다");
            throw new ClubMemberAccountStatusException(ExceptionType.CLUB_MEMBER_ACCOUNTSTATUS_REQEUST_NOT_MATCH);
        }

        log.debug("가입신청 검증 완료");

    }


}
