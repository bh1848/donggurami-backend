package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminNoticePageListResponse {
    private final List<AdminNoticeListResponse> content;
    private final int totalPages;
    private final long totalElements;
    private final int currentPage;
}
