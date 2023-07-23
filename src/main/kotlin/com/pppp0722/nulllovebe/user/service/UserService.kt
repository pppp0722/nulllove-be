package com.pppp0722.nulllovebe.user.service

import com.pppp0722.nulllovebe.global.SmsSender
import com.pppp0722.nulllovebe.user.dto.SendAuthCodeDto
import com.pppp0722.nulllovebe.user.dto.SignUpDto
import com.pppp0722.nulllovebe.user.dto.UserDto
import com.pppp0722.nulllovebe.user.entity.Auth
import com.pppp0722.nulllovebe.user.entity.User
import com.pppp0722.nulllovebe.user.repository.AuthRepository
import com.pppp0722.nulllovebe.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random


@Service
class UserService(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val smsSender: SmsSender
) {

    @Transactional
    fun signUp(signUpDto: SignUpDto): UserDto {
        userRepository.findByUserIdWithLock(signUpDto.userId)?.let{
            throw IllegalArgumentException("이미 존재하는 아이디입니다. userId: ${signUpDto.userId}")}

        val auth = authRepository.findById(signUpDto.phone)
            .orElseThrow { IllegalArgumentException("인증 정보가 존재하지 않습니다.") }

        if (auth.authCode != signUpDto.authCode) {
            throw IllegalArgumentException("인증 코드가 일치하지 않습니다.")
        }

        val user = User.fromSignUpDto(signUpDto)
        val signedUpUser = userRepository.save(user)

        return UserDto.fromEntity(signedUpUser)
    }

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
