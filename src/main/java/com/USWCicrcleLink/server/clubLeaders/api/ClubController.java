package com.USWCicrcleLink.server.clubLeaders.api;

import com.USWCicrcleLink.server.clubLeaders.dto.ClubInfoRequest;
import com.USWCicrcleLink.server.clubLeaders.service.SetClubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/club")
@RequiredArgsConstructor
public class ClubController {

    private final SetClubService setClubService;

    @PostMapping("/save-club-info")
    public ResponseEntity<Boolean> setClubInfo(@Validated ClubInfoRequest clubInfoRequest) throws IOException {
        setClubService.saveClubInfo(clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    // 재사용 가능한 파일 반환 메서드
//    private ResponseEntity<Resource> getFileResponse(String filePath) {
//        try {
//            Path file = Paths.get(filePath);
//            Resource resource = new UrlResource(file.toUri());
//            if (resource.exists() || resource.isReadable()) {
//                return ResponseEntity.ok()
//                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                        .body(resource);
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
}
