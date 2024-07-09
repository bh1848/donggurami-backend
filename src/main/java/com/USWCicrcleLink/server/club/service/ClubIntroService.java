package com.USWCicrcleLink.server.club.service;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubIntro;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.clubLeader.dto.ClubIntroRequest;
import com.USWCicrcleLink.server.club.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubIntroService {

    private final ClubIntroRepository clubIntroRepository;
    private final ClubRepository clubRepository;

    //동아리 소개글 조회
    @Transactional(readOnly = true)
    public ClubIntroResponse getClubIntroByClubId(Long clubId) {
        log.info("동아리 소개 조회 id: {}", clubId);
        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(clubId).orElseThrow(() ->
                new NoSuchElementException("해당 동아리에 대한 소개를 찾을 수 없습니다.")
        );

        RecruitmentStatus recruitmentStatus = calculateRecruitmentStatus(clubIntro);
        return new ClubIntroResponse(clubIntro, recruitmentStatus);
    }

    //동아리 모집상태 확인
    private RecruitmentStatus calculateRecruitmentStatus(ClubIntro clubIntro) {
        LocalDate today = LocalDate.now();
        if (today.isAfter(clubIntro.getRecruitmentStartDate()) && today.isBefore(clubIntro.getRecruitmentEndDate())) {
            return RecruitmentStatus.OPEN;
        }
        return RecruitmentStatus.CLOSED;
    }

}