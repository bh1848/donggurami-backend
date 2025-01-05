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
import com.USWCicrcleLink.server.global.security.util.CustomAdminDetails;
import com.USWCicrcleLink.server.global.util.validator.InputValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<ClubAdminListResponse> getAllClubs() {
        List<ClubAdminListResponse> results;
        try {
            results = clubRepository.findAllWithMemberAndLeaderCount();
        } catch (Exception e) {
            throw new ClubException(ExceptionType.ClUB_CHECKING_ERROR);
        }
        return results;
    }

    // 동아리 생성(웹)
    public ClubCreationResponse createClub(ClubCreationRequest request) {
        // 인증된 관리자 정보 가져오기
        Admin admin = getAuthenticatedAdmin();

        // 관리자 비밀번호 검증
        if (!passwordEncoder.matches(request.getAdminPw(), admin.getAdminPw())) {
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }

        // 동아리 회장 비밀번호 확인
        if (!request.getLeaderPw().equals(request.getLeaderPwConfirm())) {
            throw new ClubException(ExceptionType.ClUB_LEADER_PASSWORD_NOT_MATCH);
        }

        log.debug("동아리 회장 비밀번호 확인 성공");

        //동아리 회장, 동아리 이름 중복 확인
        validateLeaderAccount(request.getLeaderAccount());
        validateClubName(request.getClubName());

        // Club 생성 및 저장
        Club club = Club.builder()
                .clubName(InputValidator.sanitizeContent(request.getClubName()))
                .department(request.getDepartment())
                .leaderName("")
                .leaderHp("")
                .clubInsta("")
                .build();
        clubRepository.save(club);

        // Leader 생성 및 저장
        Leader leader = Leader.builder()
                .leaderAccount(InputValidator.sanitizeContent(request.getLeaderAccount()))
                .leaderPw(passwordEncoder.encode(request.getLeaderPw()))
                .leaderUUID(UUID.randomUUID())
                .role(Role.LEADER)
                .club(club)
                .build();
        leaderRepository.save(leader);

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
        log.debug("동아리 생성 성공");
        return new ClubCreationResponse(club, leader);
    }

    // 동아리 회장 아이디 중복 확인
    public void validateLeaderAccount(String leaderAccount) {
        if (leaderRepository.existsByLeaderAccount(leaderAccount)) {
            throw new ClubException(ExceptionType.LEADER_ACCOUNT_ALREADY_EXISTS);
        }
    }

    // 동아리 이름 중복 확인
    public void validateClubName(String clubName) {
        if (clubRepository.existsByClubName(clubName)) {
            throw new ClubException(ExceptionType.CLUB_NAME_ALREADY_EXISTS);
        }
    }

    // 동아리 삭제(웹)
    public void deleteClub(Long clubId, AdminPwRequest request) {

        // 인증된 관리자 정보 가져오기
        Admin admin = getAuthenticatedAdmin();

        // 관리자 비밀번호 검증
        if (!passwordEncoder.matches(request.getAdminPw(), admin.getAdminPw())) {
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }

        // 동아리 존재 여부 확인
        clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        // 동아리 및 관련 종속 엔티티와 S3 파일 삭제
        clubRepository.deleteClubAndDependencies(clubId);
        log.debug("동아리 삭제 성공: clubId = {}", clubId);
    }

    // 인증된 관리자 정보 가져오기
    private Admin getAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        return adminDetails.admin();
    }
}