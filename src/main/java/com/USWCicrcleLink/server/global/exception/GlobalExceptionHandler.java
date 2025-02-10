package com.USWCicrcleLink.server.global.exception;

import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

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

        logErrorResponse(errorResponse);
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

    // 유효하지 않은 enum값 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        // 예외 메시지에서 불필요한 부분 제거
        String rawMessage = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();

        // 필요 없는 부분 필터링하여 메시지만 추출
        String errorMessage = extractUsefulMessage(rawMessage);

        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "BAD_REQUEST",
                errorMessage,  // 메시지 반환
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

        logErrorResponse(errorResponse);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 예외 메시지에서 필요한 부분만 추출하는 메서드
    private String extractUsefulMessage(String rawMessage) {
        if (rawMessage == null) {
            return "잘못된 요청 값입니다.";
        }
        // "problem: " 이후 메시지만 추출하여 반환
        if (rawMessage.contains("problem: ")) {
            return rawMessage.substring(rawMessage.indexOf("problem: ") + 9).split("\n")[0].trim();
        }
        return rawMessage.split("\n")[0].trim(); // 첫 번째 줄만 반환
    }
}