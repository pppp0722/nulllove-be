package com.pppp0722.nulllovebe.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val message: String
) {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 예상치 못한 에러가 발생했습니다."),
    INVALID_PARAM_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 파라미터 요청입니다."),

    // 사용자 관련
    DUPLICATED_USER_ID(HttpStatus.BAD_REQUEST, "이미 존재하는 아이디입니다."),
    SMS_AUTH_FAILURE(HttpStatus.BAD_REQUEST, "SMS 인증에 실패했습니다."),
    LOGIN_DENIED(HttpStatus.UNAUTHORIZED, "이메일 혹은 비밀번호가 일치하지 않습니다."),

    // JWT 관련
    ACCESS_TOKEN_REISSUE_FAIL(HttpStatus.UNAUTHORIZED, "Access Token 재발급에 실패했습니다.");
}

class CustomException(val errorCode: ErrorCode) : RuntimeException()

class ErrorResponse(val message: String)