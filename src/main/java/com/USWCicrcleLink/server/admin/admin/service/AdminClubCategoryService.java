package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.dto.ClubCategoryCreationRequest;
import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import com.USWCicrcleLink.server.club.club.repository.ClubCategoryRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminClubCategoryService {

    private final ClubCategoryRepository clubCategoryRepository;

    // 동아리 카테고리 설정(웹) - 카테고리 추가
    public ClubCategory addCategory(ClubCategoryCreationRequest request) {
        ClubCategory category = ClubCategory.builder()
                .ClubCategory(request.getClubCategory())
                .build();
        return clubCategoryRepository.save(category);
    }

    // 동아리 카테고리 설정(웹) - 카테고리 조회
    public List<ClubCategory> getAllCategories() {
        return clubCategoryRepository.findAll();
    }

    // 동아리 카테고리 설정(웹) - 카테고리 삭제
    public void deleteCategory(Long categoryId) {
        // 카테고리 존재 여부 확인
        ClubCategory category = clubCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new BaseException(ExceptionType.CATEGORY_NOT_FOUND));

        // 카테고리 삭제
        clubCategoryRepository.delete(category);
    }
}
