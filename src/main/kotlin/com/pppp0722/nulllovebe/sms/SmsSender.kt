package com.pppp0722.nulllovebe.sms

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pppp0722.nulllovebe.global.exception.CustomException
import com.pppp0722.nulllovebe.global.exception.ErrorCode
import com.pppp0722.nulllovebe.global.properties.SmsProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class SmsSender(
    private val smsProperties: SmsProperties
) {

    private val uri = smsProperties.requestUri + smsProperties.serviceId + smsProperties.requestType
    private val url = smsProperties.domainUrl + uri

    fun sendSms(to: String, content: String) {
        val timestamp = System.currentTimeMillis().toString()
        val requestBody = mapOf(
            "type" to smsProperties.type,
            "from" to smsProperties.from,
            "content" to content,
            "messages" to listOf(
                mapOf(
                    "to" to to
                )
            )
        )

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = smsProperties.httpMethod
            doOutput = true
            doInput = true
            useCaches = false

            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("x-ncp-apigw-timestamp", timestamp)
            setRequestProperty("x-ncp-iam-access-key", smsProperties.accessKey)
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
            log.error("SMS 전송에 실패했습니다. message: {}", e.message)
            throw CustomException(ErrorCode.SMS_SEND_FAILURE)
        }
    }

    private fun makeSignature(timestamp: String): String {
        val message = "${smsProperties.httpMethod} $uri\n$timestamp\n${smsProperties.accessKey}"

        val signingKey =
            SecretKeySpec(smsProperties.secretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val rawHmac = mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))

        return Base64.getEncoder().encodeToString(rawHmac)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SmsSender::class.java)
    }
}
