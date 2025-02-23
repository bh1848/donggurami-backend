package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.dto.AdminClubCreationRequest;
import com.USWCicrcleLink.server.admin.admin.dto.AdminClubListResponse;
import com.USWCicrcleLink.server.admin.admin.dto.AdminClubPageListResponse;
import com.USWCicrcleLink.server.admin.admin.dto.AdminPwRequest;
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
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 메인 페이지(웹) - 동아리 목록 조회
     */
    @Transactional(readOnly = true)
    public AdminClubPageListResponse getAllClubs(Pageable pageable) {
        Page<AdminClubListResponse> clubs = clubRepository.findAllWithMemberAndLeaderCount(pageable);
        log.debug("동아리 목록 조회 성공 - 총 {}개", clubs.getTotalElements());

        return AdminClubPageListResponse.builder()
                .content(clubs.getContent())
                .totalPages(clubs.getTotalPages())
                .totalElements(clubs.getTotalElements())
                .currentPage(clubs.getNumber())
                .build();
    }

    /**
     * 동아리 생성(웹) - 동아리 생성 완료하기
     */
    public void createClub(AdminClubCreationRequest request) {
        Admin admin = getAuthenticatedAdmin();

        if (!request.getLeaderPw().equals(request.getLeaderPwConfirm())) {
            log.warn("[Admin: {}] 동아리 생성 실패 - 회장 비밀번호 불일치", admin.getAdminAccount());
            throw new ClubException(ExceptionType.ClUB_LEADER_PASSWORD_NOT_MATCH);
        }

        String normalizedLeaderAccount = request.getLeaderAccount().toLowerCase();
        String normalizedClubName = request.getClubName();

        validateLeaderAccount(normalizedLeaderAccount);
        validateClubName(normalizedClubName);
        validateClubRoomNumber(request.getClubRoomNumber());

        if (!passwordEncoder.matches(request.getAdminPw(), admin.getAdminPw())) {
            log.warn("[Admin: {}] 동아리 생성 실패 - 관리자 비밀번호 불일치", admin.getAdminAccount());
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }

        Club club = Club.builder()
                .clubName(normalizedClubName)
                .department(request.getDepartment())
                .clubRoomNumber(request.getClubRoomNumber())
                .build();
        clubRepository.save(club);
        log.info("[Admin: {}] 동아리 생성 성공 - Club ID: {}", admin.getAdminAccount(), club.getClubId());

        createLeaderAccount(normalizedLeaderAccount, request.getLeaderPw(), club);
        createClubDefaultData(club);
    }

    // 동아리 생성(웹) - 회장 계정 생성
    private void createLeaderAccount(String leaderAccount, String leaderPw, Club club) {
        Leader leader = Leader.builder()
                .leaderAccount(leaderAccount)
                .leaderPw(passwordEncoder.encode(leaderPw))
                .leaderUUID(UUID.randomUUID())
                .role(Role.LEADER)
                .club(club)
                .build();
        leaderRepository.save(leader);
        log.info("회장 계정 생성 성공 - Leader ID: {}", leader.getLeaderId());
    }

    // 동아리 생성(웹) - 기본 동아리 데이터 생성
    private void createClubDefaultData(Club club) {
        createClubMainPhoto(club);
        ClubIntro clubIntro = createClubIntro(club);
        createClubIntroPhotos(clubIntro);
    }

    private void createClubMainPhoto(Club club) {
        clubMainPhotoRepository.save(
                ClubMainPhoto.builder()
                        .club(club)
                        .clubMainPhotoName("")
                        .clubMainPhotoS3Key("")
                        .build()
        );
    }

    private ClubIntro createClubIntro(Club club) {
        return clubIntroRepository.save(
                ClubIntro.builder()
                        .club(club)
                        .clubIntro("")
                        .googleFormUrl("")
                        .recruitmentStatus(RecruitmentStatus.CLOSE)
                        .build()
        );
    }

    private void createClubIntroPhotos(ClubIntro clubIntro) {
        List<ClubIntroPhoto> introPhotos = List.of(
                ClubIntroPhoto.builder()
                        .clubIntro(clubIntro)
                        .clubIntroPhotoName("")
                        .clubIntroPhotoS3Key("")
                        .order(1)
                        .build(),
                ClubIntroPhoto.builder()
                        .clubIntro(clubIntro)
                        .clubIntroPhotoName("")
                        .clubIntroPhotoS3Key("")
                        .order(2)
                        .build()
        );
        clubIntroPhotoRepository.saveAll(introPhotos);
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

    // 동아리 생성(웹) - 동아리방 중복 확인
    public void validateClubRoomNumber(String clubRoomNumber) {
        if (clubRepository.existsByClubRoomNumber(clubRoomNumber)) {
            log.warn("동아리방 호수 중복 - ClubRoomNumber: {}", clubRoomNumber);
            throw new ClubException(ExceptionType.CLUB_ROOM_ALREADY_EXISTS);
        }
    }

    /**
     * 동아리 삭제(웹) - 동아리 삭제 완료하기
     */
    @Transactional
    public void deleteClub(UUID clubUUID, AdminPwRequest request) {
        Admin admin = getAuthenticatedAdmin();

        if (!passwordEncoder.matches(request.getAdminPw(), admin.getAdminPw())) {
            log.warn("[Admin: {}] 동아리 삭제 실패 - 관리자 비밀번호 불일치", admin.getAdminAccount());
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }

        Long clubId = clubRepository.findClubIdByUUID(clubUUID)
                .orElseThrow(() -> {
                    log.error("[Admin: {}] 동아리 삭제 실패 - 존재하지 않는 Club UUID: {}", admin.getAdminAccount(), clubUUID);
                    return new ClubException(ExceptionType.CLUB_NOT_EXISTS);
                });

        try {
            clubRepository.deleteClubAndDependencies(clubId);
            log.info("[Admin: {}] 동아리 삭제 성공 - Club ID: {}", admin.getAdminAccount(), clubId);
        } catch (Exception e) {
            log.error("[Admin: {}] 동아리 삭제 중 오류 발생 - Club ID: {}, 오류: {}", admin.getAdminAccount(), clubId, e.getMessage());
            throw new BaseException(ExceptionType.SERVER_ERROR, e); // 원본 예외 포함하여 다시 던지기
        }
    }

    /**
     * 인증된 관리자 정보 가져오기
     */
    private Admin getAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AdminException(ExceptionType.ADMIN_NOT_EXISTS);
        }

        if (authentication.getPrincipal() instanceof CustomAdminDetails adminDetails) {
            return adminDetails.admin();
        }

        throw new AdminException(ExceptionType.ADMIN_NOT_EXISTS);
    }

}