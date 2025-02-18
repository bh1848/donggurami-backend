package com.USWCicrcleLink.server.admin.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
@Getter
@Builder
public class AdminClubPageListResponse {
    private final List<AdminClubListResponse> content;
    private final int totalPages;
    private final long totalElements;
    private final int currentPage;
}
