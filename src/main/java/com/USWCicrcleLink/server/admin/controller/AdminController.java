package com.USWCicrcleLink.server.admin.controller;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.service.AdminService;
import com.USWCicrcleLink.server.club.domain.Club;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping
    public Club createClub(@RequestBody Club club, @RequestParam String adminPassword) {
        return adminService.createClub(club, adminPassword);
    }

    @PutMapping("/{id}")
    public Club updateClub(@PathVariable Long id, @RequestBody Club clubDetails, @RequestParam String adminPassword) {
        return adminService.updateClub(id, clubDetails, adminPassword);
    }

    @DeleteMapping("/{id}")
    public void deleteClub(@PathVariable Long id, @RequestParam String adminPassword) {
        adminService.deleteClub(id, adminPassword);
    }
}
