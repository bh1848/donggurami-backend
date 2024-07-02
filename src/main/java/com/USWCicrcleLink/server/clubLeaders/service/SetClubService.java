package com.USWCicrcleLink.server.clubLeaders.service;

import com.USWCicrcleLink.server.clubLeaders.domain.Club;
import com.USWCicrcleLink.server.clubLeaders.dto.ClubInfoRequest;
import com.USWCicrcleLink.server.clubLeaders.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

public class SetClubService {

    private final ClubRepository clubRepository;

    // 대표 사진 경로
    @Value("${file.mainPhoto-dir}")
    private String mainPhtoPath;

    // 동아리 기본 정보 저장
    public void saveClubInfo(ClubInfoRequest clubInfoRequest) throws IOException {

        Files.createDirectories(Paths.get(mainPhtoPath));

        String mainPhotoFileName = UUID.randomUUID().toString() + "_" +
                clubInfoRequest.getMainPhoto().getOriginalFilename();
        String mainPhotoPath = Paths.get(mainPhtoPath, mainPhotoFileName).toString();

        Files.copy(clubInfoRequest.getMainPhoto().getInputStream(),
                Paths.get(mainPhotoPath),
                StandardCopyOption.REPLACE_EXISTING);

        Club club = Club.builder()
                .mainPhotoPath(mainPhotoPath)
                .clubName(clubInfoRequest.getClubName())
                .leaderName(clubInfoRequest.getLeaderName())
                .department(clubInfoRequest.getDepartment())
                .chatRoomURL(clubInfoRequest.getChatRoomURL())
                .katalkID(clubInfoRequest.getKatalkID())
                .clubInsta(clubInfoRequest.getClubInsta())
                .build();

        clubRepository.save(club);
        log.info("동아리 기본 정보 저장 완료: {}", club.getClubName());
    }


}
