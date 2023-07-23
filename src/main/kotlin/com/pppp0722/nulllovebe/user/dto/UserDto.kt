package com.pppp0722.nulllovebe.user.dto

import com.pppp0722.nulllovebe.user.entity.User

data class UserDto(
    var userId: String,
    var phone: String,
    val id: Long?
) {
    companion object {
        fun fromEntity(user: User) = UserDto(user.userId, user.phone, user.id)
    }
}

data class SignUpDto(
    val userId: String,
    val password: String,
    val phone: String,
    val authCode: String
)

data class SendAuthCodeDto(
    val phone: String
)