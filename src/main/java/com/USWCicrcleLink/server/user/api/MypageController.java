//package com.USWCicrcleLink.server.user.api;
//
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
//import com.USWCicrcleLink.server.admin.notice.service.NoticeService;
//import com.USWCicrcleLink.server.global.response.ApiResponse;
//import com.USWCicrcleLink.server.user.dto.MyAplictResponse;
//import com.USWCicrcleLink.server.user.dto.MyClubResponse;
//import com.USWCicrcleLink.server.user.service.MypageService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/mypages")
//@RequiredArgsConstructor
//public class MypageController {
//
//    private final MypageService mypageService;
//    private final NoticeService noticeService;
//
//    //소속된 동아리 조회
//    @GetMapping("/my-clubs")
//    public ApiResponse<List<MyClubResponse>>getMyClubByUUID(){
//        List<MyClubResponse> myclubs = mypageService.getMyClubByUUID();
//        return new ApiResponse<>("소속된 동아리 목록 조회 성공", myclubs);
//    }
//
//    //지원한 동아리 조회
//    @GetMapping("/aplict-clubs")
//    public ApiResponse<List<MyAplictResponse>> getAplictClubByUUID(){
//        List<MyAplictResponse> aplictClubs = mypageService.getAplictClubByUUID();
//        return new ApiResponse<>("지원한 동아리 목록 조회 성공", aplictClubs);
//    }
//
//    //동아리 조회
//    @GetMapping("/notices")
//    public ResponseEntity<ApiResponse<List<NoticeListResponse>>> getAllNotices() {
//        List<NoticeListResponse> notices = noticeService.getAllNotices();
//        ApiResponse<List<NoticeListResponse>> response = new ApiResponse<>("공지사항 리스트 조회 성공", notices);
//        return ResponseEntity.ok(response);
//    }
//}
