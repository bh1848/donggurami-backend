package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.admin.notice.dto.NoticeDetailResponse;
import com.USWCicrcleLink.server.admin.notice.service.NoticeService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.dto.MyNoticeResponse;
import com.USWCicrcleLink.server.user.service.MyNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/my-notices")
@RequiredArgsConstructor
public class MyNoticeController {
    private final MyNoticeService myNoticeService;
    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MyNoticeResponse>>> getNotices(Pageable pageable) {
        List<MyNoticeResponse> notices = myNoticeService.getNotices(pageable);
        ApiResponse<List<MyNoticeResponse>> response = new ApiResponse<>("공지사항 조회 성공", notices);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{noticeId}/details")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNoticeById(@PathVariable("noticeId") Long noticeId) {
        NoticeDetailResponse notice = noticeService.getNoticeById(noticeId);
        ApiResponse<NoticeDetailResponse> response = new ApiResponse<>("공지사항 조회 성공", notice);
        return ResponseEntity.ok(response);
    }
}
