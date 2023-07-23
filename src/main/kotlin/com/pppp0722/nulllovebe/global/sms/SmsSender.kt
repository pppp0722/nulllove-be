package com.pppp0722.nulllovebe.global.sms

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pppp0722.nulllovebe.global.exception.CustomException
import com.pppp0722.nulllovebe.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class SmsSender {

    private val logger = LoggerFactory.getLogger(SmsSender::class.java)

    @Value("\${sens.domain-url}")
    private lateinit var domainUrl: String

    @Value("\${sens.request-uri}")
    private lateinit var requestUri: String

    @Value("\${sens.request-type}")
    private lateinit var requestType: String

    @Value("\${sens.http-method}")
    private lateinit var httpMethod: String

    @Value("\${sens.access-key}")
    private lateinit var accessKey: String

    @Value("\${sens.secret-key}")
    private lateinit var secretKey: String

    @Value("\${sens.service-id}")
    private lateinit var serviceId: String

    @Value("\${sens.type}")
    private lateinit var type: String

    @Value("\${sens.from}")
    private lateinit var from: String

    private val uri by lazy { requestUri + serviceId + requestType }
    private val url by lazy { domainUrl + uri }

    fun sendSms(to: String, content: String) {
        val timestamp = System.currentTimeMillis().toString()
        val requestBody = mapOf(
            "type" to type,
            "from" to from,
            "content" to content,
            "messages" to listOf(
                mapOf(
                    "to" to to
                )
            )
        )

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = httpMethod
            doOutput = true
            doInput = true
            useCaches = false

            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("x-ncp-apigw-timestamp", timestamp)
            setRequestProperty("x-ncp-iam-access-key", accessKey)
            setRequestProperty("x-ncp-apigw-signature-v2", makeSignature(timestamp))
        }

        try {
            val wr = DataOutputStream(connection.outputStream)
            wr.write(jacksonObjectMapper().writeValueAsBytes(requestBody))
            wr.flush()
            wr.close()

            connection.outputStream.close()
            connection.inputStream.close()
            connection.disconnect()
        } catch (e: Exception) {
            logger.error("SMS 전송에 실패했습니다. message: {}", e.message)
            throw CustomException(ErrorCode.SMS_SEND_FAILURE)
        }
    }

    private fun makeSignature(timestamp: String): String {
        val message = "$httpMethod $uri\n$timestamp\n$accessKey"

        val signingKey = SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val rawHmac = mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))

        return Base64.getEncoder().encodeToString(rawHmac)
    }
}
