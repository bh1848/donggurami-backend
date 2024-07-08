package com.USWCicrcleLink.server.admin.controller;

import com.USWCicrcleLink.server.admin.service.AdminService;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.clubLeaders.domain.Leader;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

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
