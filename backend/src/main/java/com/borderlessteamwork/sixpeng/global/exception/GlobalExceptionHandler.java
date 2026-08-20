package com.borderlessteamwork.sixpeng.global.exception;

import com.borderlessteamwork.sixpeng.global.response.ErrorDetail;
import com.borderlessteamwork.sixpeng.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
                .map(this::toErrorDetail)
                .toList();
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, details));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    /** 파싱 불가능한 본문(잘못된 enum 값 등)은 서버 오류가 아니라 잘못된 입력이다. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    /**
     * 쿼리 파라미터/경로 변수의 타입이 안 맞는 경우(존재하지 않는 enum 값 등).
     * 본문과 달리 HttpMessageNotReadableException 이 아니라 이 예외가 나므로 따로 받는다.
     * 처리하지 않으면 잘못된 요청이 500 으로 떨어진다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ErrorDetail detail = new ErrorDetail(
                e.getName(),
                String.valueOf(e.getValue()),
                "허용되지 않는 값입니다."
        );
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, List.of(detail)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("처리되지 않은 예외 발생", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    private ErrorDetail toErrorDetail(FieldError fieldError) {
        return new ErrorDetail(
                fieldError.getField(),
                String.valueOf(fieldError.getRejectedValue()),
                fieldError.getDefaultMessage()
        );
    }
}
