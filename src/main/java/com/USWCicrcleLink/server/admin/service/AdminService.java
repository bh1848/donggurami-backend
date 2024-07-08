package com.USWCicrcleLink.server.admin.service;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final ClubRepository clubRepository;
    
    
    //동아리 생성
    public Club createClub(Club club, String adminPassword) {
        Admin admin = adminRepository.findByAdminAccount("admin").orElse(null);
        if (admin != null && admin.getAdminPw().equals(adminPassword)) {
            return clubRepository.save(club);
        }
        throw new RuntimeException("관리자 비밀번호가 다릅니다.");
    }
    
    //동아리 업데이트
    public Club updateClub(Long id, Club clubDetails, String adminPassword) {
        Admin admin = adminRepository.findByAdminAccount("admin").orElse(null);
        if (admin != null && admin.getAdminPw().equals(adminPassword)) {
            Club club = clubRepository.findById(id).orElse(null);
            if (club != null) {
                club.setClubName(clubDetails.getClubName());
                club.setDepartment(clubDetails.getDepartment());
                return clubRepository.save(club);
            }
        }
        throw new RuntimeException("관리자 비밀번호가 다릅니다.");
    }
    
    //동아리 삭제
    public void deleteClub(Long id, String adminPassword) {
        Admin admin = adminRepository.findByAdminAccount("admin").orElse(null);
        if (admin != null && admin.getAdminPw().equals(adminPassword)) {
            clubRepository.deleteById(id);
        } else {
            throw new RuntimeException("관리자 비밀번호가 다릅니다.");
        }
    }
}
