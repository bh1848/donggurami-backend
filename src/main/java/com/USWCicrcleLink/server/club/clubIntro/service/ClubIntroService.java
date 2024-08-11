package com.USWCicrcleLink.server.club.clubIntro.service;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.dto.ClubByDepartmentResponse;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubIntroException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubIntroService {

    private final ClubIntroRepository clubIntroRepository;
    private final ClubRepository clubRepository;

    //분과별 동아리 조회(모바일)
    @Transactional(readOnly = true)
    public List<ClubByDepartmentResponse> getClubsByDepartment(Department department) {
        log.debug("분과별 동아리 조회: {}", department);
        List<Club> clubs = clubRepository.findByDepartment(department);
        if (clubs.isEmpty()) {
            throw new ClubException(ExceptionType.CLUB_NOT_EXISTS);
        }
        return clubs.stream()
                .map(ClubByDepartmentResponse::new)
                .collect(Collectors.toList());
    }

    //모집 상태에 따른 분과별 동아리 조회(모바일)
    @Transactional(readOnly = true)
    public List<ClubByDepartmentResponse> getClubsByRecruitmentStatusAndDepartment(RecruitmentStatus recruitmentStatus, Department department) {
        log.debug("모집 상태 및 분과별 동아리 조회: recruitmentStatus={}, department={}", recruitmentStatus, department);
        List<Club> clubs = clubRepository.findByRecruitmentStatusAndDepartment(recruitmentStatus, department);
        if (clubs.isEmpty()) {
            throw new ClubException(ExceptionType.CLUB_NOT_EXISTS);
        }
        return clubs.stream()
                .map(ClubByDepartmentResponse::new)
                .collect(Collectors.toList());
    }

    //동아리 소개글 조회(모바일)
    @Transactional(readOnly = true)
    public ClubIntroResponse getClubIntroByClubId(Long clubId) {
        log.debug("동아리 소개 조회 id: {}", clubId);
        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(clubId).orElseThrow(() ->
                new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

        Club club = clubIntro.getClub();
        return new ClubIntroResponse(clubIntro, club.getRecruitmentStatus());
    }
}