package com.USWCicrcleLink.server.admin.notice.api;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeCreationRequest;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeDetailResponse;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.admin.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    //공지사항 전체 리스트 조회
    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<List<NoticeListResponse>>> getAllNotices() {
        List<NoticeListResponse> notices = noticeService.getAllNotices();
        ApiResponse<List<NoticeListResponse>> response = new ApiResponse<>("공지사항 리스트 조회 성공", notices);
        return ResponseEntity.ok(response);
    }

    //공지사항 리스트 조회(페이징)
    @GetMapping("/notices/paged")
    public ResponseEntity<PagedModel<NoticeListResponse>> getNotices(Pageable pageable, PagedResourcesAssembler<Notice> pagedResourcesAssembler) {
        PagedModel<NoticeListResponse> pagedNotices = noticeService.getNotices(pageable, pagedResourcesAssembler);
        return ResponseEntity.ok(pagedNotices);
    }

    //공지사항 내용 조회
    @GetMapping("/notice/get/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNoticeById(@PathVariable("noticeId") Long noticeId) {
        NoticeDetailResponse notice = noticeService.getNoticeById(noticeId);
        ApiResponse<NoticeDetailResponse> response = new ApiResponse<>("공지사항 조회 성공", notice);
        return ResponseEntity.ok(response);
    }

    //공지사항 생성
    @PostMapping("/notice/create")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> createNotice(@RequestHeader("admin_id") Long adminId, @RequestBody NoticeCreationRequest request) {
        NoticeDetailResponse createdNotice = noticeService.createNotice(request, adminId);
        ApiResponse<NoticeDetailResponse> response = new ApiResponse<>("공지사항 생성 성공", createdNotice);
        return ResponseEntity.ok(response);
    }

    //공지사항 수정
    @PatchMapping("/notice/update/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> updateNotice(@PathVariable("noticeId") Long noticeId, @RequestBody NoticeCreationRequest request) {
        NoticeDetailResponse updatedNotice = noticeService.updateNotice(noticeId, request);
        ApiResponse<NoticeDetailResponse> response = new ApiResponse<>("공지사항 수정 성공", updatedNotice);
        return ResponseEntity.ok(response);
    }

    //공지사항 삭제
    @DeleteMapping("/notice/delete/{noticeId}")
    public ResponseEntity<ApiResponse<Long>> deleteNotice(@PathVariable("noticeId") Long noticeId) {
        noticeService.deleteNotice(noticeId);
        ApiResponse<Long> response = new ApiResponse<>("공지사항 삭제 성공", noticeId);
        return ResponseEntity.ok(response);
    }
}
