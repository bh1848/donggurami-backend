package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginRequest;
import com.USWCicrcleLink.server.admin.admin.dto.ClubCreationRequest;
import com.USWCicrcleLink.server.admin.admin.dto.ClubDetailResponse;
import com.USWCicrcleLink.server.admin.admin.dto.ClubListResponse;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.util.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final ClubRepository clubRepository;
    private final ClubIntroRepository clubIntroRepository;
    private final LeaderRepository leaderRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final AplictRepository aplictRepository;
    private final JwtProvider jwtProvider;

    // 관리자 로그인(웹)
    public TokenDto adminLogin(HttpServletRequest request, AdminLoginRequest loginRequest) {
        log.info("관리자 로그인 요청: {}", loginRequest.getAdminAccount());
        Admin admin = adminRepository.findByAdminAccount(loginRequest.getAdminAccount())
                .orElseThrow(() -> new RuntimeException("아이디나 비밀번호를 확인해주세요."));

        if (!admin.getAdminPw().equals(loginRequest.getAdminPw())) {
            throw new RuntimeException("아이디나 비밀번호를 확인해주세요.");
        }

        log.info("JWT 생성");
        String accessToken = jwtProvider.createAccessToken(admin.getAdminUUID().toString(), admin.getRole(), List.of());

        request.setAttribute(JwtProvider.AUTHORIZATION_HEADER, JwtProvider.BEARER_PREFIX + accessToken);

        log.info("로그인 성공, 엑세스 토큰: {}", accessToken);
        return new TokenDto(accessToken);
    }

    // 동아리 목록 조회(웹)
    public List<ClubListResponse> getAllClubs(HttpServletRequest request) {
        String token = jwtProvider.resolveAccessToken(request);
        jwtProvider.validateAccessToken(token);  // 토큰 검증

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
    public ClubDetailResponse getClubById(HttpServletRequest request, Long clubId) {
        String token = jwtProvider.resolveAccessToken(request);
        jwtProvider.validateAccessToken(token);  // 토큰 검증

        Club club = clubRepository.findById(clubId).orElseThrow(() -> new RuntimeException("해당 동아리를 찾을 수 없습니다."));
        ClubIntro clubIntro = clubIntroRepository.findByClub(club).orElse(null);

        return new ClubDetailResponse(club, clubIntro);
    }

    // 동아리 생성(웹)
    public void createClub(HttpServletRequest request, ClubCreationRequest clubRequest) {
        String token = jwtProvider.resolveAccessToken(request);
        jwtProvider.validateAccessToken(token);  // 토큰 검증

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        Admin admin = adminDetails.admin();

        if (admin.getAdminPw().equals(clubRequest.getAdminPw())) {
            if (!clubRequest.getLeaderPw().equals(clubRequest.getLeaderPwConfirm())) {
                throw new RuntimeException("동아리 회장 비밀번호가 일치하지 않습니다.");
            }

            Leader leader = Leader.builder()
                    .leaderAccount(clubRequest.getLeaderAccount())
                    .leaderPw(clubRequest.getLeaderPw())
                    .build();
            leaderRepository.save(leader);

            Club club = Club.builder()
                    .clubName(clubRequest.getClubName())
                    .department(clubRequest.getDepartment())
                    .leaderName(clubRequest.getLeaderAccount())
                    .recruitmentStatus(RecruitmentStatus.CLOSE)
                    .build();
            clubRepository.save(club);

            ClubIntro clubIntro = ClubIntro.builder()
                    .club(club)
                    .clubIntro("")
                    .clubIntroPhotoPath("")
                    .additionalPhotoPath1("")
                    .additionalPhotoPath2("")
                    .googleFormUrl("")
                    .build();
            clubIntroRepository.save(clubIntro);
        } else {
            throw new RuntimeException("관리자 비밀번호를 확인해주세요");
        }
    }

    // 동아리 삭제(웹)
    public void deleteClub(HttpServletRequest request, Long clubId, String adminPw) {
        String token = jwtProvider.resolveAccessToken(request);
        jwtProvider.validateAccessToken(token);  // 토큰 검증

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        Admin admin = adminDetails.admin();

        if (admin.getAdminPw().equals(adminPw)) {
            // 종속된 엔티티를 먼저 삭제
            aplictRepository.deleteByClubClubId(clubId); // Aplict 데이터 삭제
            clubIntroRepository.deleteByClubClubId(clubId); // ClubIntro 데이터 삭제
            clubMembersRepository.deleteByClubClubId(clubId); // ClubMembers 데이터 삭제
            leaderRepository.deleteByClubClubId(clubId); // Leader 데이터 삭제

            clubRepository.deleteById(clubId); // Club 데이터 삭제
        } else {
            throw new RuntimeException("관리자 비밀번호를 확인해주세요");
        }
    }
}
