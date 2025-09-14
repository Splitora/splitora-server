package com.satwik.splitora.exception;

import com.satwik.splitora.constants.enums.ErrorCode;
import com.satwik.splitora.persistence.dto.ErrorDetails;
import com.satwik.splitora.persistence.dto.ErrorResponseModel;
import com.satwik.splitora.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseModel> handleBadRequestException(RuntimeException ex) {
        return buildBadRequestResponse(ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseModel> handleInvalidArgumentException(MethodArgumentNotValidException ex) {
        return handleValidationExceptions(ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseModel> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation exception occurred: ", ex);
        String message = ex.getMessage();
        if(message.contains("uq_user_email")) {
            message = "Email already exists";
        } else if(message.contains("uq_user_username")) {
            message = "Username already exists";
        } else {
            message = "Data integrity violation";
        }

        ErrorResponseModel errorResponse = ResponseUtil.error(message, HttpStatus.BAD_REQUEST, new ErrorDetails(
                ErrorCode.INVALID_REQUEST.getCode(),
                ErrorCode.INVALID_REQUEST.getMessage()
        ));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorResponseModel> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation exception occurred: ", ex);

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponseModel errorResponse = ResponseUtil.error("Invalid fields value!", HttpStatus.BAD_REQUEST, new ErrorDetails(
                ErrorCode.INVALID_REQUEST.getCode(),
                ErrorCode.INVALID_REQUEST.getMessage()
        ), errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorResponseModel> buildBadRequestResponse(Exception ex) {
        log.error("Runtime exception occurred: ", ex);
        ErrorResponseModel errorResponse = ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST, new ErrorDetails(
                ErrorCode.INVALID_REQUEST.getCode(),
                ErrorCode.INVALID_REQUEST.getMessage()
        ));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<ErrorResponseModel> handleRefreshTokenInvalidException(RefreshTokenInvalidException ex) {
        log.error("RefreshTokenInvalidException occurred: ", ex);
        ErrorResponseModel errorResponse = ResponseUtil.error("Refresh Token Invalid", HttpStatus.UNAUTHORIZED, new ErrorDetails(
                ErrorCode.REFRESH_TOKEN_INVALID.getCode(),
                ErrorCode.REFRESH_TOKEN_INVALID.getMessage()
        ));

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponseModel> handleDataNotFoundException(DataNotFoundException ex) {
        log.error("DataNotFoundException occurred: ", ex);
        ErrorResponseModel errorResponse = ResponseUtil.error(ex.getMessage(), HttpStatus.NOT_FOUND, new ErrorDetails(
                ErrorCode.ENTITY_NOT_FOUND.getCode(),
                ErrorCode.ENTITY_NOT_FOUND.getMessage()
        ));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FailedToSaveException.class)
    public ResponseEntity<ErrorResponseModel> handleFailedToSaveException(FailedToSaveException ex) {
        log.error("FailedToSaveException occurred: ", ex);
        ErrorResponseModel errorResponse = ResponseUtil.error("Failed to Save", HttpStatus.BAD_REQUEST, new ErrorDetails(
                ErrorCode.ENTITY_NOT_FOUND.getCode(),
                ErrorCode.ENTITY_NOT_FOUND.getMessage()
        ));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseModel> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("AccessDeniedException occurred: ", ex);
        ErrorResponseModel errorResponse = ResponseUtil.error("Access Denied", HttpStatus.FORBIDDEN, new ErrorDetails(
                ErrorCode.ACCESS_DENIED.getCode(),
                ErrorCode.ACCESS_DENIED.getMessage()
        ));

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseModel> handleGeneralException(Exception ex) {
        log.error("ServiceCommunicationException handled with message: ", ex);
        ErrorResponseModel errorResponse = ResponseUtil.error("Unknown Error", HttpStatus.INTERNAL_SERVER_ERROR, new ErrorDetails(
                ErrorCode.UNKNOWN_ERROR.getCode(),
                ErrorCode.UNKNOWN_ERROR.getMessage()
        ));
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
