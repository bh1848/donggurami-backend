package com.USWCicrcleLink.server.clubLeaders.service;

import com.USWCicrcleLink.server.clubLeaders.domain.Club;
import com.USWCicrcleLink.server.clubLeaders.domain.ClubIntro;
import com.USWCicrcleLink.server.clubLeaders.domain.Leader;
import com.USWCicrcleLink.server.clubLeaders.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.clubLeaders.dto.ClubIntroRequest;
import com.USWCicrcleLink.server.clubLeaders.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.clubLeaders.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeaders.repository.ClubRepository;
import com.USWCicrcleLink.server.clubLeaders.repository.LeaderRepository;
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
    private final LeaderRepository leaderRepository;
    private final ClubRepository clubRepository;
    @Value("${file.introPhoto-dir}")
    private String introPhotoDir;
    @Value("#{'${file.allowed-extensions}'.split(',')}")
    private List<String> allowedExtensions;

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


    public void writeClubIntro(ClubIntroRequest clubIntroRequest) throws IOException {

        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(clubIntroRequest.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        // 사진 파일 업로드 과정
        createUploadDir();// 사진 파일 디렉터리 없는 경우 생성

        // 기존 파일 경로가 있는지
        ClubIntro existingClubIntro = clubIntroRepository.findByClub(club).orElse(null);

        // 파일 있나 ? 덮어쓰기 : 비워두기
        String introPhotoPath = saveFile(clubIntroRequest.getIntroPhoto(),
                existingClubIntro != null ? existingClubIntro.getIntroPhotoPath() : null);

        String additionalPhotoPath1 = saveFile(clubIntroRequest.getAdditionalPhoto1(),
                existingClubIntro != null ? existingClubIntro.getAdditionalPhotoPath1() : null);

        String additionalPhotoPath2 = saveFile(clubIntroRequest.getAdditionalPhoto2(),
                existingClubIntro != null ? existingClubIntro.getAdditionalPhotoPath2() : null);

        // 동아리 소개 저장
        ClubIntro clubIntro = ClubIntro.builder()
                .club(club)
                .introContent(clubIntroRequest.getClubIntro())
                .introPhotoPath(introPhotoPath)
                .additionalPhotoPath1(additionalPhotoPath1)
                .additionalPhotoPath2(additionalPhotoPath2)
                .build();

        clubIntroRepository.save(clubIntro);
        log.info("동아리 소개 저장 완료: {}", clubIntro);
    }

    // 사진 파일 저장 디렉터리 없는 경우 생성
    private void createUploadDir() throws IOException {
        Files.createDirectories(Paths.get(introPhotoDir));
    }

    // 사진 파일 저장 후 파일 경로 리턴
    private String saveFile(MultipartFile file, String existingFilePath) throws IOException {

        // 파일이 없거나 넣지 않은 경우
        if (file == null || file.isEmpty()) {
            return existingFilePath;
        }

        // 기존 파일 삭제
        if (existingFilePath != null) {
            deleteFile(Path.of(existingFilePath));
        }

        // 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // 지원하는 확장자인지 검증
        validateFileExtension(extension);

        // UUID 이용해서 파일 이름 생성
        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
        String filePath = Paths.get(introPhotoDir, uniqueFileName).toString();
        Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        return filePath;
    }

    // 기존 파일 삭제
    private void deleteFile(Path existingFilePath) throws IOException {
        if (Files.exists(existingFilePath)) {
            Files.delete(existingFilePath);
        }
    }

    // 파일 확장자 추출
    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf('.');

        // 없으면 빈 값으로 있으면 .이후에 확장자를 추출
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    // 지원하는 확장자인지 검증
    private void validateFileExtension(String extension) throws IOException {
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IOException("지원하지 않는 파일 확장자입니다.: " + extension);
        }
    }
}