package org.dzhabarov.naujavaproject.handler;

import lombok.extern.slf4j.Slf4j;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Глобальная обработка исключений для REST API
 */
@Slf4j
@ControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    /** Ошибка бизнес-логики (400) */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseEntity<String> handleBusinessExceptionForApi(BusinessException ex) {
        log.warn("Business error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Ошибка: " + ex.getMessage());
    }

    /** Ресурс не найден (404) */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Ошибка: " + ex.getMessage());
    }

    /** Непредвиденная ошибка (500) */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ошибка: " + ex.getMessage());
    }
}
