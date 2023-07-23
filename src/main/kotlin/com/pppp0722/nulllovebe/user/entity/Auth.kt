package com.pppp0722.nulllovebe.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash(timeToLive = 180)
data class Auth(
    @Id
    val phone: String,
    val authCode: String
)