package com.USWCicrcleLink.server.club.controller;

import com.USWCicrcleLink.server.club.dto.ClubResponse;
import com.USWCicrcleLink.server.club.service.ClubService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController {
    private final ClubService clubService;

    //모든 동아리 조회
    @GetMapping
    public ResponseEntity<ApiResponse> getAllClubs() {
        List<ClubResponse> clubs = clubService.getAllClubs();
        ApiResponse response = new ApiResponse("모든 동아리 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //동아리 조회
    @GetMapping("/{clubId}")
    public ResponseEntity<ApiResponse> getClubById(@PathVariable("clubId") Long id) {
        ClubResponse club = clubService.getClubById(id);
        ApiResponse response = new ApiResponse("동아리 조회 성공", club);
        return ResponseEntity.ok(response);
    }

//    //분과별 동아리 조회
//    @GetMapping("/department/{department}")
//    public ResponseEntity<ApiResponse> getClubsByDepartment(@PathVariable("department") Department department) {
//        List<ClubByDepartmentResponse> clubs = clubService.getClubsByDepartment(department);
//        ApiResponse response = new ApiResponse("분과별 동아리 조회 성공", clubs);
//        return ResponseEntity.ok(response);
//    }
//
//    //해당동아리지원서조회
//    @GetMapping("/aplict/{clubId}")
//    public ResponseEntity<ApiResponse> getAplictByClubId(@PathVariable("clubId") Long clubId) {
//        List<AplictResponse> aplicts = clubService.getAplictByClubId(clubId);
//        ApiResponse response = new ApiResponse("지원서 조회 성공", aplicts);
//        return ResponseEntity.ok(response);
//    }
}