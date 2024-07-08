package com.USWCicrcleLink.server.admin.controller;

import com.USWCicrcleLink.server.admin.service.AdminService;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.clubLeaders.domain.Leader;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    //동아리 전체 리스트 조회
    @GetMapping("/clubs")
    public ResponseEntity<ApiResponse> getAllClubs() {
        List<Club> clubs = adminService.getAllClubs();
        ApiResponse response = new ApiResponse("동아리 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //동아리 페이징 리스트 조회
    @GetMapping("/clubs/paged")
    public ResponseEntity<ApiResponse> getClubs(Pageable pageable) {
        Page<Club> clubs = adminService.getClubs(pageable);
        ApiResponse response = new ApiResponse("동아리 페이징 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //동아리 생성
    @PostMapping("/club/create")
    public ResponseEntity<ApiResponse> createClub(@RequestBody Club club, @RequestBody Leader leader, @RequestBody String adminPassword) {
        adminService.createClub(club, leader, adminPassword);
        ApiResponse response = new ApiResponse("동아리 생성 성공", club);
        return ResponseEntity.ok(response);
    }

    //동아리 삭제
    @DeleteMapping("/club/delete/{clubId}")
    public ResponseEntity<ApiResponse> deleteClub(@PathVariable Long clubId, @RequestBody String adminPassword) {
        adminService.deleteClub(clubId, adminPassword);
        ApiResponse response = new ApiResponse("동아리 삭제 성공", clubId);
        return ResponseEntity.ok(response);
    }
}
