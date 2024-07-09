package com.USWCicrcleLink.server.club.api;

import com.USWCicrcleLink.server.club.dto.ClubInfoRequest;
import com.USWCicrcleLink.server.club.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController {
    private final ClubService clubService;

    @PatchMapping("/update")
    public ResponseEntity<Boolean> updateClubInfo(@Validated ClubInfoRequest clubInfoRequest) throws IOException {
        clubService.updateClubInfo(clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}