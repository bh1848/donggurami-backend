package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.dto.ClubCategoryCreationRequest;
import com.USWCicrcleLink.server.admin.admin.mapper.ClubCategoryMapper;
import com.USWCicrcleLink.server.admin.admin.dto.ClubCategoryResponse;
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

    // 동아리 카테고리 설정(웹) - 카테고리 조회
    public List<ClubCategoryResponse> getAllCategories() {
        List<ClubCategory> categories = clubCategoryRepository.findAll();
        log.info("동아리 카테고리 조회 성공: {}개 카테고리 반환", categories.size());

        return ClubCategoryMapper.toDtoList(categories);
    }

    // 동아리 카테고리 설정(웹) - 카테고리 추가
    public ClubCategoryResponse addCategory(ClubCategoryCreationRequest request) {
        // 중복 확인
        clubCategoryRepository.findByClubCategoryName(request.getClubCategoryName())
                .ifPresent(category -> {
                    throw new BaseException(ExceptionType.DUPLICATE_CATEGORY);
                });

        // 새 카테고리 생성 및 저장
        ClubCategory category = ClubCategory.builder()
                .clubCategoryName(request.getClubCategoryName())
                .build();

        ClubCategory savedCategory = clubCategoryRepository.save(category);
        log.info("동아리 카테고리 추가 성공: ID={}, Name={}", savedCategory.getClubCategoryId(), savedCategory.getClubCategoryName());

        return ClubCategoryMapper.toDto(savedCategory);
    }

    // 동아리 카테고리 설정(웹) - 카테고리 삭제
    public ClubCategoryResponse deleteCategory(Long categoryId) {
        // 카테고리 존재 여부 확인
        ClubCategory category = clubCategoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("동아리 카테고리 삭제 실패: 카테고리 ID={}를 찾을 수 없음", categoryId);
                    return new BaseException(ExceptionType.CATEGORY_NOT_FOUND);
                });

        // 카테고리 삭제
        clubCategoryRepository.delete(category);
        log.info("동아리 카테고리 삭제 성공: ID={}", categoryId);

        // 삭제된 카테고리 정보를 응답으로 반환
        return ClubCategoryMapper.toDto(category);
    }
}
