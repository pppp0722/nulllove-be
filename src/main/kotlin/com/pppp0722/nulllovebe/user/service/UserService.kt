package com.pppp0722.nulllovebe.user.service

import com.pppp0722.nulllovebe.global.exception.CustomException
import com.pppp0722.nulllovebe.global.exception.ErrorCode
import com.pppp0722.nulllovebe.global.sms.SmsSender
import com.pppp0722.nulllovebe.user.dto.SendAuthCodeDto
import com.pppp0722.nulllovebe.user.dto.SignUpDto
import com.pppp0722.nulllovebe.user.dto.UserDto
import com.pppp0722.nulllovebe.user.entity.Auth
import com.pppp0722.nulllovebe.user.entity.User
import com.pppp0722.nulllovebe.user.repository.AuthRepository
import com.pppp0722.nulllovebe.user.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.random.Random


@Service
class UserService(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val smsSender: SmsSender
) {
    @Transactional
    fun signUp(signUpDto: SignUpDto): UserDto {
        if (userRepository.existsByUserId(signUpDto.userId)) {
            throw CustomException(ErrorCode.DUPLICATED_USER_ID)
        }

        val auth = authRepository.findById(signUpDto.phone)
        if(!isAuthenticated(auth, signUpDto.authCode)) {
            throw CustomException(ErrorCode.SMS_AUTH_FAILURE)
        }

        val user = User.fromSignUpDto(signUpDto)

        val signedUpUser = try {
            userRepository.save(user)
        } catch (e: DataIntegrityViolationException) {
            throw CustomException(ErrorCode.DUPLICATED_USER_ID)
        }

        return UserDto.fromEntity(signedUpUser)
    }

    private fun isAuthenticated(auth: Optional<Auth>, authCode: String) =
        auth.isPresent && (auth.get().authCode == authCode)

    fun sendAuthCode(sendAuthCodeDto: SendAuthCodeDto) {
        val authCode = generateRandomAuthCode()
        val content = "[널좋아해] 인증번호 [${authCode}]를 입력해주세요."
        smsSender.sendSms(sendAuthCodeDto.phone, content)

        val auth = Auth(sendAuthCodeDto.phone, authCode)
        authRepository.save(auth)
    }

    private fun generateRandomAuthCode() =
        (1..authCodeLength)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

    companion object {
        private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        private const val authCodeLength = 6
    }
}
