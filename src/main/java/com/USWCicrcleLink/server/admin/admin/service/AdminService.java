package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.dto.*;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.util.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final JwtProvider jwtProvider;
    private final AdminRepository adminRepository;
    private final LeaderRepository leaderRepository;
    private final ClubRepository clubRepository;
    private final ClubIntroRepository clubIntroRepository;

    // 동아리 목록 조회(웹)
    public List<ClubListResponse> getAllClubs() {
        List<ClubListResponse> results;
        try {
            results = clubRepository.findAllWithMemberAndLeaderCount();
        } catch (Exception e) {
            throw new RuntimeException("동아리 조회 중 에러 발생", e);
        }
        return results;
    }

    // 동아리 상세 페이지 조회(웹)
    @Transactional(readOnly = true)
    public ClubDetailResponse getClubById(Long clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow(() -> new RuntimeException("해당 동아리를 찾을 수 없습니다."));
        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(club.getClubId()).orElse(null);

        return new ClubDetailResponse(club, clubIntro);
    }

    // 동아리 생성(웹)
    public ClubCreationResponse createClub(ClubCreationRequest clubRequest) {
        log.info("동아리 생성 요청 시작");
        try {
            // SecurityContextHolder에서 인증 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
            Admin admin = adminDetails.admin();

            if (admin.getAdminPw().equals(clubRequest.getAdminPw())) {
                log.info("관리자 비밀번호 확인 성공");

                if (!clubRequest.getLeaderPw().equals(clubRequest.getLeaderPwConfirm())) {
                    throw new RuntimeException("동아리 회장 비밀번호가 일치하지 않습니다.");
                }

                log.info("동아리 회장 비밀번호 확인 성공");

                Leader leader = Leader.builder()
                        .leaderAccount(clubRequest.getLeaderAccount())
                        .leaderPw(clubRequest.getLeaderPw())
                        .role(Role.LEADER)
                        .build();
                leaderRepository.save(leader);
                log.info("동아리 회장 생성 성공: {}", leader.getLeaderAccount());

                Club club = Club.builder()
                        .clubName(clubRequest.getClubName())
                        .department(clubRequest.getDepartment())
                        .leaderName(clubRequest.getLeaderAccount())
                        .recruitmentStatus(RecruitmentStatus.CLOSE)
                        .build();
                clubRepository.save(club);
                log.info("동아리 생성 성공: {}", club.getClubName());

                ClubIntro clubIntro = ClubIntro.builder()
                        .club(club)
                        .clubIntro("")
                        .clubIntroPhotoPath("")
                        .additionalPhotoPath1("")
                        .additionalPhotoPath2("")
                        .googleFormUrl("")
                        .build();
                clubIntroRepository.save(clubIntro);
                log.info("동아리 소개 생성 성공: {}", clubIntro.getClubIntro());

                return new ClubCreationResponse(club);
            } else {
                log.warn("관리자 비밀번호 확인 실패");
                throw new RuntimeException("관리자 비밀번호를 확인해주세요");
            }
        } catch (Exception e) {
            log.error("동아리 생성 중 오류 발생", e);
            throw e;
        }
    }

    // 동아리 삭제(웹)
    public void deleteClub(Long clubId, String adminPw) {
        log.info("동아리 삭제 요청 시작: clubId = {}", clubId);
        try {
            // SecurityContextHolder에서 인증 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
            Admin admin = adminDetails.admin();
            log.info("인증된 관리자: {}", admin.getAdminAccount());

            if (admin.getAdminPw().equals(adminPw)) {
                log.info("관리자 비밀번호 확인 성공");

                // 종속 엔티티 삭제
                clubRepository.deleteClubAndDependencies(clubId);
                log.info("동아리 삭제 성공: clubId = {}", clubId);
            } else {
                log.warn("관리자 비밀번호 확인 실패");
                throw new RuntimeException("관리자 비밀번호를 확인해주세요");
            }
        } catch (Exception e) {
            log.error("동아리 삭제 중 오류 발생", e);
            throw e;
        }
    }
}