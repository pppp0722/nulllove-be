package com.pppp0722.nulllovebe.global.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("sms")
data class SmsProperties(
    val domainUrl: String,
    val requestUri: String,
    val requestType: String,
    val httpMethod: String,
    val accessKey: String,
    val secretKey: String,
    val serviceId: String,
    val type: String,
    val from: String
)