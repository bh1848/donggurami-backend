package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.dto.MyNoticeResponse;
import com.USWCicrcleLink.server.user.service.MyNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/my-notices")
@RequiredArgsConstructor
public class MyNoticeController {
    private final MyNoticeService myNoticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MyNoticeResponse>>> getNotices(Pageable pageable) {
        List<MyNoticeResponse> notices = myNoticeService.getNotices(pageable);
        ApiResponse<List<MyNoticeResponse>> response = new ApiResponse<>("공지사항 조회 성공", notices);
        return ResponseEntity.ok(response);
    }

}
