package com.pppp0722.nulllovebe.user.dto

import com.pppp0722.nulllovebe.user.entity.User

data class UserDto(
    var userId: String,
    var phone: String,
    var love: String?,
    val id: Long?
) {

    companion object {
        fun fromEntity(user: User) = UserDto(user.userId, user.phone, user.love, user.id)
    }
}

data class ExistsDto(
    val exists: Boolean
)

data class SendAuthCodeDto(
    val phone: String
)

data class SignUpDto(
    val userId: String,
    val password: String,
    val phone: String,
    val authCode: String
)

data class LoginDto(
    val userId: String,
    val password: String
)

data class UpdateDto(
    val love: String
)

data class LoveInfoDto(
    val matches: Boolean,
    val loverCount: Int
)