package com.USWCicrcleLink.server.admin.service;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.dto.ClubCreationRequest;
import com.USWCicrcleLink.server.admin.dto.ClubDetailDto;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubIntro;
import com.USWCicrcleLink.server.club.domain.Leader;
import com.USWCicrcleLink.server.club.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.repository.LeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    //동아리 전체 리스트 조회
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    //동아리 페이징 리스트 조회
    public Page<Club> getClubs(Pageable pageable) {
        return clubRepository.findAll(pageable);
    }

    //동아리 상세 페이지 조회
    public ClubDetailDto getClubById(Long clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow(() -> new RuntimeException("클럽을 찾을 수 없습니다."));
        ClubIntro clubIntro = clubIntroRepository.findByClub(club).orElse(null);

        return ClubDetailDto.builder()
                .clubName(club.getClubName())
                .leaderName(club.getLeaderName())
                .phone(club.getKatalkID())
                .instagram(club.getClubInsta())
                .mainPhotoPath(club.getMainPhotoPath())
                .chatRoomUrl(club.getChatRoomUrl())
                .introContent(clubIntro != null ? clubIntro.getIntroContent() : "")
                .build();
    }

    // 동아리 생성
    public void createClub(ClubCreationRequest request) {
        Admin admin = adminRepository.findByAdminAccount("admin").orElse(null);
        if (admin != null && admin.getAdminPw().equals(request.getAdminPw())) {
            if (!request.getLeaderPw().equals(request.getLeaderPwConfirm())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
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
                    .build();
            clubRepository.save(club);

            ClubIntro clubIntro = ClubIntro.builder()
                    .club(club)
                    .introContent("")
                    .introPhotoPath("")
                    .additionalPhotoPath1("")
                    .additionalPhotoPath2("")
                    .googleFormUrl("")
                    .build();
            clubIntroRepository.save(clubIntro);

            return;
        }
        throw new RuntimeException("비밀번호를 확인해주세요");
    }

    //동아리 삭제
    public void deleteClub(Long id, String adminPassword) {
        Admin admin = adminRepository.findByAdminAccount("admin").orElse(null);
        if (admin != null && admin.getAdminPw().equals(adminPassword)) {
            clubRepository.deleteById(id);
        } else {
            throw new RuntimeException("비밀번호를 확인해주세요");
        }
    }
}