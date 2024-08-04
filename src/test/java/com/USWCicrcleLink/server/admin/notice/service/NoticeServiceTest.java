//package com.USWCicrcleLink.server.admin.notice.service;
//
//import com.USWCicrcleLink.server.admin.admin.domain.Admin;
//import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
//import com.USWCicrcleLink.server.admin.notice.domain.Notice;
//import com.USWCicrcleLink.server.admin.notice.domain.NoticePhoto;
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeCreationRequest;
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeDetailResponse;
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponseAssembler;
//import com.USWCicrcleLink.server.admin.notice.repository.NoticePhotoRepository;
//import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
//import com.USWCicrcleLink.server.global.util.FileUploadService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.web.PagedResourcesAssembler;
//import org.springframework.hateoas.PagedModel;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class NoticeServiceTest {
//
//    @Mock
//    private NoticeRepository noticeRepository;
//
//    @Mock
//    private NoticeListResponseAssembler noticeListResponseAssembler;
//
//    @Mock
//    private AdminRepository adminRepository;
//
//    @Mock
//    private NoticePhotoRepository noticePhotoRepository;
//
//    @Mock
//    private FileUploadService fileUploadService;
//
//    @Mock
//    private PagedResourcesAssembler<Notice> pagedResourcesAssembler;
//
//    @InjectMocks
//    private NoticeService noticeService;
//
//    private Admin admin;
//    private Notice notice;
//
//    @BeforeEach
//    void setUp() {
//        admin = Admin.builder().adminId(1L).adminName("관리자").build();
//        notice = Notice.builder()
//                .noticeId(1L)
//                .noticeTitle("제목")
//                .noticeContent("내용")
//                .noticeCreatedAt(LocalDateTime.now())
//                .admin(admin)
//                .build();
//    }
//
//    @Test
//    void 공지사항_전체_리스트_조회() {
//        //given
//        when(noticeRepository.findAll()).thenReturn(Collections.singletonList(notice));
//
//        //when
//        List<NoticeListResponse> responses = noticeService.getAllNotices();
//
//        //then
//        assertEquals(1, responses.size());
//        verify(noticeRepository, times(1)).findAll();
//    }
//
//    @Test
//    void 공지사항_리스트_조회_페이징() {
//        //given
//        Page<Notice> noticePage = new PageImpl<>(Collections.singletonList(notice));
//        when(noticeRepository.findAll(any(PageRequest.class))).thenReturn(noticePage);
//        when(pagedResourcesAssembler.toModel(isA(Page.class), isA(NoticeListResponseAssembler.class)))
//                .thenReturn(PagedModel.empty());
//
//        //when
//        PagedModel<NoticeListResponse> response = noticeService.getNotices(PageRequest.of(0, 10), pagedResourcesAssembler);
//
//        //then
//        assertNotNull(response);
//        verify(noticeRepository, times(1)).findAll(any(PageRequest.class));
//    }
//
//    @Test
//    void 공지사항_내용_조회() {
//        //given
//        when(noticeRepository.findById(anyLong())).thenReturn(Optional.of(notice));
//        when(noticePhotoRepository.findByNotice(any(Notice.class))).thenReturn(Collections.emptyList());
//
//        //when
//        NoticeDetailResponse response = noticeService.getNoticeById(1L);
//
//        //then
//        assertNotNull(response);
//        assertEquals(notice.getNoticeTitle(), response.getNoticeTitle());
//        verify(noticeRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    void 공지사항_생성() throws IOException {
//        //given
//        NoticeCreationRequest request = new NoticeCreationRequest();
//        request.setNoticeTitle("새제목");
//        request.setNoticeContent("새내용");
//
//        when(adminRepository.findById(anyLong())).thenReturn(Optional.of(admin));
//        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);
//        Mockito.lenient().when(fileUploadService.saveFile(any(MultipartFile.class), any(), anyString())).thenReturn("path/to/photo");
//
//        //when
//        NoticeDetailResponse response = noticeService.createNotice(1L, request, new MultipartFile[0]);
//
//        //then
//        assertNotNull(response);
//        assertEquals(notice.getNoticeTitle(), response.getNoticeTitle());
//        verify(noticeRepository, times(1)).save(any(Notice.class));
//    }
//
//    @Test
//    void 공지사항_수정() throws IOException {
//        //given
//        NoticeCreationRequest request = new NoticeCreationRequest();
//        request.setNoticeTitle("제목수정");
//        request.setNoticeContent("내용수정");
//
//        when(noticeRepository.findById(anyLong())).thenReturn(Optional.of(notice));
//        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);
//        when(noticePhotoRepository.findByNotice(any(Notice.class))).thenReturn(Collections.emptyList());
//
//        //when
//        NoticeDetailResponse response = noticeService.updateNotice(1L, request, new MultipartFile[0]);
//
//        //then
//        assertNotNull(response);
//        assertEquals("제목수정", response.getNoticeTitle());
//        verify(noticeRepository, times(1)).findById(1L);
//        verify(noticeRepository, times(1)).save(any(Notice.class));
//    }
//
//    @Test
//    void 공지사항_삭제() {
//        //given
//        when(noticeRepository.findById(anyLong())).thenReturn(Optional.of(notice));
//        when(noticePhotoRepository.findByNotice(any(Notice.class))).thenReturn(Collections.emptyList());
//
//        //when
//        noticeService.deleteNotice(1L);
//
//        //then
//        verify(noticeRepository, times(1)).delete(any(Notice.class));
//    }
//}
