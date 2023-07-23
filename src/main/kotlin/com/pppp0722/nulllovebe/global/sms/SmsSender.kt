package com.pppp0722.nulllovebe.global.sms

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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

    @Value("\${sens.domain-url}")
    private lateinit var domainUrl: String
    @Value("\${sens.request-uri}")
    private lateinit var requestUri: String
    @Value("\${sens.request-type}")
    private lateinit var requestType: String
    @Value("\${sens.request-method}")
    private lateinit var requestMethod: String
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

        with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = this@SmsSender.requestMethod
            doOutput = true
            doInput = true
            useCaches = false

            setRequestProperty("Content-Type", "application/json; charset=utf-8");
            setRequestProperty("x-ncp-apigw-timestamp", timestamp)
            setRequestProperty("x-ncp-iam-access-key", accessKey)
            setRequestProperty("x-ncp-apigw-signature-v2", makeSignature(timestamp))

            val wr = DataOutputStream(outputStream)
            wr.write(jacksonObjectMapper().writeValueAsBytes(requestBody))
            wr.flush()
            wr.close()

            outputStream.close()
            inputStream.close()
            disconnect()
        }
    }

    private fun makeSignature(timestamp: String): String {
        val message = "$requestMethod $uri\n$timestamp\n$accessKey"

        val signingKey = SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val rawHmac = mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))

        return Base64.getEncoder().encodeToString(rawHmac)
    }
}
