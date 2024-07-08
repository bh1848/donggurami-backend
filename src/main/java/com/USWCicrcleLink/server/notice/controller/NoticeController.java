package com.USWCicrcleLink.server.notice.controller;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.notice.domain.Notice;
import com.USWCicrcleLink.server.notice.dto.NoticeResponse;
import com.USWCicrcleLink.server.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    //공지사항 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse> getAllNotices() {
        List<NoticeResponse> notices = noticeService.getAllNotices();
        ApiResponse response = new ApiResponse("공지사항 목록 조회 성공", notices);
        return ResponseEntity.ok(response);
    }
    
    //공지사항 페이징 목록 조회
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse> getNotices(Pageable pageable) {
        Page<NoticeResponse> notices = noticeService.getNotices(pageable);
        ApiResponse response = new ApiResponse("공지사항 페이징 목록 조회 성공", notices);
        return ResponseEntity.ok(response);
    }

    //공지사항 내용 조회
    @GetMapping("/get/{noticeId}")
    public ResponseEntity<ApiResponse> getNoticeById(@PathVariable("noticeId") Long noticeId) {
        NoticeResponse notice = noticeService.getNoticeById(noticeId);
        ApiResponse response;
        response = new ApiResponse("공지사항 조회 성공", notice);
        return ResponseEntity.ok(response);

    }

    //공지사항 생성
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createNotice(@RequestBody Notice notice) {
        NoticeResponse createdNotice = noticeService.createNotice(notice);
        ApiResponse response = new ApiResponse("공지사항 생성 성공", createdNotice);
        return ResponseEntity.ok(response);
    }

    //공지사항 수정
    @PatchMapping("/update/{noticeId}")
    public ResponseEntity<ApiResponse> updateNotice(@PathVariable("noticeId") Long noticeId, @RequestBody Notice notice) {
        NoticeResponse updatedNotice = noticeService.updateNotice(noticeId, notice);
        ApiResponse response;
        response = new ApiResponse("공지사항 수정 성공", updatedNotice);
        return ResponseEntity.ok(response);
    }

    //공지사항 삭제
    @DeleteMapping("/delete/{noticeId}")
    public ResponseEntity<ApiResponse> deleteNotice(@PathVariable("noticeId") Long noticeId) {
        noticeService.deleteNotice(noticeId);
        ApiResponse response = new ApiResponse("공지사항 삭제 성공", noticeId);
        return ResponseEntity.ok(response);
    }
}