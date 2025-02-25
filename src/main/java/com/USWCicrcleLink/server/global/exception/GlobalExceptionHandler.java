package com.USWCicrcleLink.server.global.exception;

import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 공통 ErrorResponse 생성
    private ErrorResponse buildErrorResponse(String exception, String code, String message, HttpStatus status, Object additionalData) {
        return ErrorResponse.builder()
                .exception(exception)
                .code(code)
                .message(message)
                .status(status.value())
                .error(status.getReasonPhrase())
                .additionalData(additionalData)
                .build();
    }

    // 공통 로그 처리
    private void logErrorResponse(ErrorResponse errorResponse) {
        log.error("code : {}, message : {}, additionalData : {}",
                errorResponse.getCode(),
                errorResponse.getMessage(),
                errorResponse.getAdditionalData());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "NO_CATCH_ERROR",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
        );

        logErrorResponse(errorResponse);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        ExceptionType exceptionType = e.getExceptionType();
        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                exceptionType.getCode(),
                exceptionType.getMessage(),
                exceptionType.getStatus(),
                e.getAdditionalData()
        );

        return new ResponseEntity<>(errorResponse, exceptionType.getStatus());
    }

    // NotBlank 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // UUID 변환 예외 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleUUIDTypeMismatch(MethodArgumentTypeMismatchException ex) {

        Throwable rootCause = NestedExceptionUtils.getRootCause(ex);
        String errorMessage = (rootCause instanceof IllegalArgumentException)
                ? "유효하지 않은 UUID 형식입니다. 올바른 UUID를 입력하세요."
                : "잘못된 요청입니다.";

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getClass().getSimpleName(),
                "INVALID_UUID_FORMAT",
                errorMessage,
                HttpStatus.BAD_REQUEST,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * JsonCreator 예외 처리 때문에 기본 spring 예외 던짐.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {

        Throwable rootCause = NestedExceptionUtils.getRootCause(e);
        String errorMessage = (rootCause instanceof IllegalArgumentException)
                ? rootCause.getMessage()
                : "유효하지 않은 ENUM 값입니다. 올바른 값을 입력하세요.";

        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "INVALID_ENUM_FORMAT",
                errorMessage,
                HttpStatus.BAD_REQUEST,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // NPE 처리
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {

        Throwable rootCause = NestedExceptionUtils.getRootCause(e);
        String errorMessage = (rootCause != null)
                ? "NullPointerException 발생: " + rootCause.getMessage()
                : "잘못된 요청입니다. 필수 값을 확인하세요.";

        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "NULL_POINTER_EXCEPTION",
                errorMessage,
                HttpStatus.BAD_REQUEST,
                null
        );

        logErrorResponse(errorResponse);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 잘못된 경로로 요청시 반환
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
        ErrorResponse errorResponse = buildErrorResponse(
                "NoResourceFoundException",
                "RESOURCE_NOT_FOUND",
                "요청하신 경로를 찾을 수 없습니다.",
                HttpStatus.NOT_FOUND,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 파일 크기 10MB 초과시
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "MAX_UPLOAD_SIZE_EXCEEDED",
                "업로드 가능한 최대 파일 크기를 초과했습니다. (개별 파일 10MB, 총 파일 크기 50MB)",
                HttpStatus.BAD_REQUEST,
                null
        );

        logErrorResponse(errorResponse);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}