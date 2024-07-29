package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.dto.MyAplictResponse;
import com.USWCicrcleLink.server.user.dto.MyClubResponse;
import com.USWCicrcleLink.server.user.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mypages")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    //소속된 동아리 조회
    @GetMapping("/{uuid}/my-clubs")
    public ApiResponse<List<MyClubResponse>>getMyClubByUUID(@PathVariable UUID uuid){
        List<MyClubResponse> myclubs = mypageService.getMyClubByUUID(uuid);
        return new ApiResponse<>("소속된 동아리 목록 조회 성공", myclubs);
    }

    //지원한 동아리 조회
    @GetMapping("/{uuid}/aplict-clubs")
    public ApiResponse<List<MyAplictResponse>> getAplictClubByUUID(@PathVariable UUID uuid){
        List<MyAplictResponse> aplictClubs = mypageService.getAplictClubByUUID(uuid);
        return new ApiResponse<>("지원한 동아리 목록 조회 성공", aplictClubs);
    }

}
