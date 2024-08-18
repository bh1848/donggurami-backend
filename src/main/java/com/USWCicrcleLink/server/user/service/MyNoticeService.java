package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
import com.USWCicrcleLink.server.global.security.util.CustomUserDetails;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.MyNoticeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MyNoticeService {
    private final NoticeRepository noticeRepository;

    public List<MyNoticeResponse> getNotices(Pageable pageable) {
        Page<MyNoticeResponse> pagedResults = noticeRepository.findAll(pageable)
                .map(MyNoticeResponse::from);
        return pagedResults.getContent(); // 페이지네이션 정보 없이 데이터만 반환
    }
}

