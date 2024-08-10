package com.USWCicrcleLink.server.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
@AllArgsConstructor
public enum ExceptionType {
//    100-199: 인증 및 권한
//    200-299: 사용자 관리
//    300-399: 데이터 관리
//    400-499: 토큰 및 인증
//    500-599: 메일 및 통신

    /**
     * SERVER ERROR
     */
    SERVER_ERROR("SER-501", "서버 오류 입니다. 관리자에게 문의해주세요", INTERNAL_SERVER_ERROR),

    /**
     * Domain: EmailToken
     */
    EMAIL_TOKEN_NOT_FOUND("EMAIL_TOKEN-001", "해당 토큰이 존재하지 않습니다.", BAD_REQUEST),
    EMAIL_TOKEN_IS_EXPIRED("EMAIL_TOKEN-002", "토큰이 만료되었습니다. 다시 이메일인증 해주세요", BAD_REQUEST),


    /**
     * Domain: User
     */
    USER_NOT_EXISTS("USR-201", "사용자가 존재하지 않습니다.", BAD_REQUEST),
    USER_NEW_PASSWORD_NOT_MATCH("USR-202","새 비밀번호와  새 비밀번호 확인이 일치하지 않습니다.", BAD_REQUEST),
    USER_PASSWORD_NOT_INPUT("USR-203","비밀번호 값이 빈칸입니다",BAD_REQUEST),
    USER_PASSWORD_NOT_MATCH("USR-204","현재 비밀번호와 일치하지 않습니다",BAD_REQUEST),
    USER_PASSWORD_UPDATE_FAIL("USR-205","비밀번호 업데이트에 실패했습니다",BAD_REQUEST),
    USER_OVERLAP("USR-206", "이미 존재하는 회원 입니다.", INTERNAL_SERVER_ERROR),
    USER_ACCOUNT_OVERLAP("USR-207", "계정이 중복됩니다.", INTERNAL_SERVER_ERROR),
    USER_ACCOUNT_NOT_EXISTS("USR-208", "존재하지 않는 계정입니다.", BAD_REQUEST),
    USER_INVALID_ACCOUNT_AND_EMAIL("USR-209", "올바르지 않은 이메일 혹은 아이디 입니다.", BAD_REQUEST),
    USER_UUID_NOT_FOUND("USR-210","회원의 uuid를 찾을 수 없습니다.",BAD_REQUEST),
    USER_AUTHENTICATION_FAILED("USR-211","아이디 혹은 비밀번호가 일치하지 않습니다",BAD_REQUEST),
    USER_PASSWORD_MISMATCH("USR-212", "두 비밀번호가 일치하지 않습니다", BAD_REQUEST),



    /**
     * Domain : Profile
     */
    PROFILE_NOT_EXISTS("PFL-201", "프로필이 존재하지 않습니다.", BAD_REQUEST),
    PROFILE_UPDATE_FAIL("PFL-202", "프로필 업데이트에 실패했습니다.", BAD_REQUEST),

    /**
     * Domain : ClubMember
     */
    CLUB_MEMBER_NOT_EXISTS("CMEM-201","클럽멤버가 존재하지 않습니다.",BAD_REQUEST),

    /**
     * Domain : Aplict
     */
    APLICT_NOT_EXISTS("APT_201","지원서가 존재하지 않습니다.",BAD_REQUEST),

    /**
     * Domain : VerificationCode
     */
    INVALID_AUTH_CODE("VC-101", "인증번호가 일치하지 않습니다", BAD_REQUEST),

    /**
     * 공통
     */
    SEND_MAIL_FAILED("EML-501", "메일 전송에 실패했습니다.", INTERNAL_SERVER_ERROR);





    private final String code;
    private final String message;
    private final HttpStatus status;
}
