package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.dto.*;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.repository.ClubMainPhotoRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.AdminException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminClubService {

    private final LeaderRepository leaderRepository;
    private final ClubRepository clubRepository;
    private final ClubIntroRepository clubIntroRepository;
    private final ClubMainPhotoRepository clubMainPhotoRepository;
    private final ClubIntroPhotoRepository clubIntroPhotoRepository;
    private final PasswordEncoder passwordEncoder;

    // 동아리 목록 조회(웹)
    public Page<AdminClubListResponse> getAllClubs(Pageable pageable) {
        log.debug("동아리 목록 조회 요청 - 페이지 정보: {}", pageable);
        try {
            Page<AdminClubListResponse> result = clubRepository.findAllWithMemberAndLeaderCount(pageable);
            log.debug("동아리 목록 조회 성공 - 총 {}개", result.getTotalElements());
            return result;
        } catch (Exception e) {
            log.error("동아리 목록 조회 실패", e);
            throw new ClubException(ExceptionType.ClUB_CHECKING_ERROR);
        }
    }

    // 동아리 생성(웹) - 동아리 생성 완료하기
    public void createClub(ClubCreationRequest request) {
        // 인증된 관리자 정보 가져오기
        Admin admin = getAuthenticatedAdmin();
        log.debug("동아리 생성 요청 - 관리자 ID: {}, 동아리명: {}", admin.getAdminId(), request.getClubName());

        // 동아리 회장 비밀번호 확인
        if (!request.getLeaderPw().equals(request.getLeaderPwConfirm())) {
            log.warn("동아리 생성 실패 - 회장 비밀번호 불일치");
            throw new ClubException(ExceptionType.ClUB_LEADER_PASSWORD_NOT_MATCH);
        }

        //동아리 회장, 동아리 이름 중복 확인
        validateLeaderAccount(request.getLeaderAccount());
        validateClubName(request.getClubName());

        // 관리자 비밀번호 검증
        if (!passwordEncoder.matches(request.getAdminPw(), admin.getAdminPw())) {
            log.warn("동아리 생성 실패 - 관리자 비밀번호 불일치, 관리자 ID: {}", admin.getAdminId());
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }

        // Club 생성 및 저장
        Club club = Club.builder()
                .clubName(request.getClubName())
                .department(request.getDepartment())
                .leaderName("")
                .leaderHp("")
                .clubInsta("")
                .clubRoomNumber(request.getClubRoomNumber())
                .build();
        clubRepository.save(club);
        log.info("동아리 생성 성공 - Club ID: {}, 동아리명: {}", club.getClubId(), club.getClubName());

        // Leader 생성 및 저장
        Leader leader = Leader.builder()
                .leaderAccount(request.getLeaderAccount())
                .leaderPw(passwordEncoder.encode(request.getLeaderPw()))
                .leaderUUID(UUID.randomUUID())
                .role(Role.LEADER)
                .club(club)
                .build();
        leaderRepository.save(leader);
        log.info("회장 계정 생성 성공 - Leader ID: {}, 계정명: {}", leader.getLeaderId(), leader.getLeaderAccount());

        // ClubMainPhoto 생성 및 저장
        ClubMainPhoto mainPhoto = ClubMainPhoto.builder()
                .club(club)
                .clubMainPhotoName("")
                .clubMainPhotoS3Key("")
                .build();
        clubMainPhotoRepository.save(mainPhoto);

        // ClubIntro 생성 및 저장
        ClubIntro clubIntro = ClubIntro.builder()
                .club(club)
                .clubIntro("")
                .googleFormUrl("")
                .recruitmentStatus(RecruitmentStatus.CLOSE)
                .build();
        clubIntroRepository.save(clubIntro);

        // ClubIntroPhoto 기본값 설정 (5개 생성)
        List<ClubIntroPhoto> introPhotos = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ClubIntroPhoto introPhoto = ClubIntroPhoto.builder()
                    .clubIntro(clubIntro)
                    .clubIntroPhotoName("")
                    .clubIntroPhotoS3Key("")
                    .order(i)
                    .build();
            introPhotos.add(introPhoto);
        }
        clubIntroPhotoRepository.saveAll(introPhotos);
        log.info("동아리 생성 및 초기 데이터 저장 완료 - Club ID: {}", club.getClubId());
    }

    // 동아리 생성(웹) - 동아리 회장 아이디 중복 확인
    public void validateLeaderAccount(String leaderAccount) {
        if (leaderRepository.existsByLeaderAccount(leaderAccount)) {
            log.warn("동아리 회장 계정 중복 - LeaderAccount: {}", leaderAccount);
            throw new ClubException(ExceptionType.LEADER_ACCOUNT_ALREADY_EXISTS);
        }
    }

    // 동아리 생성(웹) - 동아리 이름 중복 확인
    public void validateClubName(String clubName) {
        if (clubRepository.existsByClubName(clubName)) {
            log.warn("동아리명 중복 - ClubName: {}", clubName);
            throw new ClubException(ExceptionType.CLUB_NAME_ALREADY_EXISTS);
        }
    }

    // 동아리 삭제(웹) - 동아리 삭제 완료하기
    public void deleteClub(Long clubId, AdminPwRequest request) {

        // 인증된 관리자 정보 가져오기
        Admin admin = getAuthenticatedAdmin();
        log.info("동아리 삭제 요청 - 관리자 ID: {}, 동아리 ID: {}", admin.getAdminId(), clubId);

        // 관리자 비밀번호 검증
        if (!passwordEncoder.matches(request.getAdminPw(), admin.getAdminPw())) {
            log.warn("동아리 삭제 실패 - 관리자 비밀번호 불일치, 관리자 ID: {}", admin.getAdminId());
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }

        // 동아리 존재 여부 확인
        clubRepository.findById(clubId)
                .orElseThrow(() -> {
                    log.error("동아리 삭제 실패 - 존재하지 않는 Club ID: {}", clubId);
                    return new ClubException(ExceptionType.CLUB_NOT_EXISTS);
                });

        // 동아리 및 관련 데이터 삭제
        clubRepository.deleteClubAndDependencies(clubId);
        log.info("동아리 삭제 성공 - Club ID: {}", clubId);
    }

    // 인증된 관리자 정보 가져오기
    private Admin getAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        return adminDetails.admin();
    }
}