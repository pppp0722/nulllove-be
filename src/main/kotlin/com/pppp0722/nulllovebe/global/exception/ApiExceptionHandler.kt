package com.pppp0722.nulllovebe.global.exception

import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(CustomException::class)
    private fun handleLoginDeniedException(e: CustomException): ResponseEntity<ErrorResponse> {
        return handleExceptionInternal(e.errorCode)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    private fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        logger.warn(e.message)
        return handleExceptionInternal(ErrorCode.INVALID_PARAM_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    private fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error(e.message)
        return handleExceptionInternal(ErrorCode.INTERNAL_SERVER_ERROR)
    }

    private fun handleExceptionInternal(errorCode: ErrorCode): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(errorCode.message)
        return ResponseEntity.status(errorCode.httpStatus).body(errorResponse)
    }
}
