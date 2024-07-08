package com.USWCicrcleLink.server.clubLeaders.service;

import com.USWCicrcleLink.server.clubLeaders.domain.ClubIntro;
import com.USWCicrcleLink.server.clubLeaders.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.clubLeaders.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.clubLeaders.repository.ClubIntroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubIntroService {
    private final ClubIntroRepository clubIntroRepository;

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