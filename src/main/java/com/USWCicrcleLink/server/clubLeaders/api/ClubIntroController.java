package com.USWCicrcleLink.server.clubLeaders.api;

import com.USWCicrcleLink.server.clubLeaders.dto.ClubIntroRequest;
import com.USWCicrcleLink.server.clubLeaders.service.ClubIntroService;
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
@RequestMapping("/club-intro")
@RequiredArgsConstructor
public class ClubIntroController {

    private final ClubIntroService clubIntroService;

    @PostMapping("/save")
    public ResponseEntity<Boolean> setClubInfo(@Validated ClubIntroRequest clubInfoRequest) throws IOException {
        clubIntroService.writeClubIntro(clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
