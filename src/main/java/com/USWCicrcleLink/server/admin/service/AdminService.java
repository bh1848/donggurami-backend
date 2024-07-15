package com.USWCicrcleLink.server.admin.service;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.dto.*;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubIntro;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    //관리자 로그인
    public void adminLogin(AdminLoginRequest request) {
        Admin admin = adminRepository.findByAdminAccount(request.getAdminAccount()).orElseThrow(() -> new RuntimeException("아이디나 비밀번호를 확인해주세요"));
        if (!admin.getAdminPw().equals(request.getAdminPw())) {
            throw new RuntimeException("아이디나 비밀번호를 확인해주세요.");
        }
    }

    //동아리 전체 목록 조회
    public List<ClubListResponse> getAllClubs() {
        return clubRepository.findAll().stream()
                .map(this::toClubListResponse)
                .collect(Collectors.toList());
    }

    private ClubListResponse toClubListResponse(Club club) {
        return ClubListResponse.builder()
                .clubId(club.getClubId())
                .department(club.getDepartment())
                .clubName(club.getClubName())
                .leaderName(club.getLeaderName())
                .numberOfClubMembers(clubMembersRepository.countByClub(club))
                .build();
    }

    //동아리 상세 페이지 조회
    public ClubDetailResponse getClubById(Long clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow(() -> new RuntimeException("해당 동아리를 찾을 수 없습니다."));
        ClubIntro clubIntro = clubIntroRepository.findByClub(club).orElse(null);

        return ClubDetailResponse.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .leaderName(club.getLeaderName())
                .phone(club.getKatalkID())
                .instagram(club.getClubInsta())
                .mainPhotoPath(club.getMainPhotoPath())
                .chatRoomUrl(club.getChatRoomUrl())
                .introContent(clubIntro != null ? clubIntro.getClubIntro() : "")
                .build();
    }

    //동아리 생성
    public void createClub(ClubCreationRequest request) {
        Admin admin = adminRepository.findByAdminAccount("admin").orElse(null);
        if (admin != null && admin.getAdminPw().equals(request.getAdminPw())) {
            if (!request.getLeaderPw().equals(request.getLeaderPwConfirm())) {
                throw new RuntimeException("동아리 회장 비밀번호가 일치하지 않습니다.");
            }

            Leader leader = Leader.builder()
                    .leaderAccount(request.getLeaderAccount())
                    .leaderPw(request.getLeaderPw())
                    .build();
            leaderRepository.save(leader);

            Club club = Club.builder()
                    .clubName(request.getClubName())
                    .department(request.getDepartment())
                    .leaderName(request.getLeaderAccount())
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

            return;
        }
        throw new RuntimeException("관리자 비밀번호를 확인해주세요");
    }

    //동아리 삭제
    public void deleteClub(Long clubId, String adminPw) {
        Admin admin = adminRepository.findByAdminAccount("admin").orElse(null);
        if (admin != null && admin.getAdminPw().equals(adminPw)) {
            clubIntroRepository.deleteByClubClubId(clubId); //참조된 ClubIntro 데이터 삭제
            clubRepository.deleteById(clubId); //Club 데이터 삭제
        } else {
            throw new RuntimeException("관리자 비밀번호를 확인해주세요");
        }
    }
}