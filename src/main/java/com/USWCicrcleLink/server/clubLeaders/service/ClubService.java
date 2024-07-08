package com.USWCicrcleLink.server.clubLeaders.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.dto.AplictResponse;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.clubLeaders.domain.Club;
import com.USWCicrcleLink.server.clubLeaders.domain.Department;
import com.USWCicrcleLink.server.clubLeaders.dto.ClubByDepartmentResponse;
import com.USWCicrcleLink.server.clubLeaders.dto.ClubResponse;
import com.USWCicrcleLink.server.clubLeaders.repository.ClubRepository;
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
public class ClubService {
    private final ClubRepository clubRepository;
    private final AplictRepository aplictRepository;

    //모든 동아리 조회
    @Transactional(readOnly = true)
    public List<ClubResponse> getAllClubs(){
        log.info("모든 동아리 조회");
        List<Club> clubs = clubRepository.findAll();
        if (clubs.isEmpty()) {
            throw new NoSuchElementException("동아리가 없습니다.");
        }
        return clubs.stream().map(ClubResponse::new).collect(Collectors.toList());
    }

    //동아리 조회
    @Transactional(readOnly = true)
    public ClubResponse getClubById(Long id) {
        log.info("동아리 조회 id: {}", id);
        Club club = clubRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 ID를 가진 동아리를 찾을 수 없습니다.")
        );
        return new ClubResponse(club);
    }

    //분과별 동아리 조회
    @Transactional(readOnly = true)
    public List<ClubByDepartmentResponse> getClubsByDepartment(Department department) {
        log.info("분과별 동아리 조회: {}", department);
        List<Club> clubs = clubRepository.findByDepartment(department);
        if (clubs.isEmpty()) {
            throw new NoSuchElementException("해당 분과에 속하는 동아리가 없습니다.");
        }
        return clubs.stream()
                .map(ClubByDepartmentResponse::new)
                .collect(Collectors.toList());
    }

    //해당동아리 지원서 조회
    public List<AplictResponse> getAplictByClubId(Long clubId) {
        List<Aplict> aplicts = aplictRepository.findByClub(clubRepository.findById(clubId).orElseThrow(() -> new IllegalArgumentException("동아리를 찾을 수 없습니다.")));
        return aplicts.stream()
                .map(AplictResponse::from)
                .collect(Collectors.toList());
    }
}