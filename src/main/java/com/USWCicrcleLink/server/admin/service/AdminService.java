package com.USWCicrcleLink.server.admin.service;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.clubLeaders.domain.Leader;
import com.USWCicrcleLink.server.clubLeaders.repository.LeaderRepository;
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
    private final LeaderRepository leaderRepository;

    //동아리 생성
    public void createClub(Club club, Leader leader, String adminPassword) {
        Admin admin = adminRepository.findByAdminAccount("admin").orElse(null);
        if (admin != null && admin.getAdminPw().equals(adminPassword)) {
            Leader savedLeader = leaderRepository.save(leader);
            club.setLeader(savedLeader);
            clubRepository.save(club);
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