package com.USWCicrcleLink.server.notice.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.notice.dto.NoticeCreationRequest;
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
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> getAllNotices() {
        List<NoticeResponse> notices = noticeService.getAllNotices();
        ApiResponse<List<NoticeResponse>> response = new ApiResponse<>("공지사항 목록 조회 성공", notices);
        return ResponseEntity.ok(response);
    }

    //공지사항 페이징 목록 조회
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> getNotices(Pageable pageable) {
        Page<NoticeResponse> notices = noticeService.getNotices(pageable);
        ApiResponse<Page<NoticeResponse>> response = new ApiResponse<>("공지사항 페이징 목록 조회 성공", notices);
        return ResponseEntity.ok(response);
    }

    //공지사항 내용 조회
    @GetMapping("/get/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNoticeById(@PathVariable("noticeId") Long noticeId) {
        NoticeResponse notice = noticeService.getNoticeById(noticeId);
        ApiResponse<NoticeResponse> response = new ApiResponse<>("공지사항 조회 성공", notice);
        return ResponseEntity.ok(response);
    }

    //공지사항 생성
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(@RequestBody NoticeCreationRequest request) {
        NoticeResponse createdNotice = noticeService.createNotice(request);
        ApiResponse<NoticeResponse> response = new ApiResponse<>("공지사항 생성 성공", createdNotice);
        return ResponseEntity.ok(response);
    }

    //공지사항 수정
    @PatchMapping("/update/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(@PathVariable("noticeId") Long noticeId, @RequestBody NoticeCreationRequest request) {
        NoticeResponse updatedNotice = noticeService.updateNotice(noticeId, request);
        ApiResponse<NoticeResponse> response = new ApiResponse<>("공지사항 수정 성공", updatedNotice);
        return ResponseEntity.ok(response);
    }

    //공지사항 삭제
    @DeleteMapping("/delete/{noticeId}")
    public ResponseEntity<ApiResponse<Long>> deleteNotice(@PathVariable("noticeId") Long noticeId) {
        noticeService.deleteNotice(noticeId);
        ApiResponse<Long> response = new ApiResponse<>("공지사항 삭제 성공", noticeId);
        return ResponseEntity.ok(response);
    }
}
