package com.pppp0722.nulllovebe.user.service

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.exceptions.JWTVerificationException
import com.pppp0722.nulllovebe.global.exception.CustomException
import com.pppp0722.nulllovebe.global.exception.ErrorCode
import com.pppp0722.nulllovebe.jwt.JwtGenerator
import com.pppp0722.nulllovebe.sms.SmsSender
import com.pppp0722.nulllovebe.user.dto.LoginDto
import com.pppp0722.nulllovebe.user.dto.SendAuthCodeDto
import com.pppp0722.nulllovebe.user.dto.SignUpDto
import com.pppp0722.nulllovebe.user.dto.UserDto
import com.pppp0722.nulllovebe.user.entity.Auth
import com.pppp0722.nulllovebe.user.entity.RefreshToken
import com.pppp0722.nulllovebe.user.entity.User
import com.pppp0722.nulllovebe.user.repository.AuthRepository
import com.pppp0722.nulllovebe.user.repository.RefreshTokenRepository
import com.pppp0722.nulllovebe.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.random.Random

@Service
class UserService(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val smsSender: SmsSender,
    private val jwtGenerator: JwtGenerator,
    private val jwtVerifier: JWTVerifier,
    private val passwordEncoder: PasswordEncoder
) {

    private val log = LoggerFactory.getLogger(UserService::class.java)

    @Transactional(readOnly = true)
    fun existsByUserId(userId: String) = userRepository.existsByUserId(userId)

    @Transactional(readOnly = true)
    fun existsByPhone(phone: String) = userRepository.existsByPhone(phone)

    @Transactional(readOnly = true)
    fun sendAuthCode(sendAuthCodeDto: SendAuthCodeDto) {
        if (userRepository.existsByPhone(sendAuthCodeDto.phone)) {
            throw CustomException(ErrorCode.DUPLICATED_PHONE)
        }

        authRepository.findById(sendAuthCodeDto.phone)
            .ifPresent { throw CustomException(ErrorCode.ALREADY_SENT_PHONE) }

        val authCode = generateRandomAuthCode()
        val content = "[널좋아해] 인증번호 [${authCode}]를 입력해주세요."
        smsSender.sendSms(sendAuthCodeDto.phone, content)

        val auth = Auth(sendAuthCodeDto.phone, authCode)
        authRepository.save(auth)

        log.info("SMS 인증 발송 완료. phone: {}", auth.phone)
    }

    private fun generateRandomAuthCode() =
        (1..authCodeLength)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

    @Transactional
    fun signUp(signUpDto: SignUpDto) {
        if (userRepository.existsByUserId(signUpDto.userId)) {
            throw CustomException(ErrorCode.DUPLICATED_USER_ID)
        }

        val auth = authRepository.findById(signUpDto.phone)
        if (!isAuthenticated(auth, signUpDto.authCode)) {
            throw CustomException(ErrorCode.SMS_AUTH_FAILURE)
        }

        val user = User.fromSignUpDto(signUpDto, passwordEncoder)

        val signedUpUser = try {
            userRepository.save(user)
        } catch (e: DataIntegrityViolationException) {
            throw CustomException(ErrorCode.DUPLICATED_USER_ID)
        }

        val userDto = UserDto.fromEntity(signedUpUser)
        log.info("회원가입 완료. userDto: {}", userDto)
    }

    private fun isAuthenticated(auth: Optional<Auth>, authCode: String) =
        auth.isPresent && (auth.get().authCode == authCode)

    @Transactional(readOnly = true)
    fun login(loginDto: LoginDto): String {
        val user = userRepository.findByUserId(loginDto.userId)
            ?: throw CustomException(ErrorCode.LOGIN_DENIED)

        if (!passwordEncoder.matches(loginDto.password, user.password)) {
            throw CustomException(ErrorCode.LOGIN_DENIED)
        }

        val refreshToken = jwtGenerator.generateRefreshToken(user.userId, arrayOf(user.role))
        refreshTokenRepository.save(RefreshToken(user.userId, refreshToken))

        return refreshToken;
    }

    fun reissueAccessToken(refreshToken: String): String {
        if (refreshToken.isEmpty()) {
            log.warn("Access Token 미존재")
            throw CustomException(ErrorCode.ACCESS_TOKEN_REISSUE_FAIL)
        }

        val decodedRefreshToken = try {
            jwtVerifier.verify(refreshToken)
        } catch (e: JWTVerificationException) {
            log.warn("Refresh Token 검증 실패 : {}", e.message)
            throw CustomException(ErrorCode.ACCESS_TOKEN_REISSUE_FAIL)
        }

        val userId = decodedRefreshToken.audience[0]
        val savedRefreshToken = refreshTokenRepository.findById(userId)
            .orElseThrow {
                log.warn("저장되어있지 않은 Refresh Token 사용")
                CustomException(ErrorCode.ACCESS_TOKEN_REISSUE_FAIL)
            }

        if (savedRefreshToken.value != refreshToken) {
            log.warn("비정상으로 발급된 Refresh Token 사용")
            throw CustomException(ErrorCode.ACCESS_TOKEN_REISSUE_FAIL)
        }

        val roles = decodedRefreshToken.getClaim("roles").asArray(String::class.java)

        return jwtGenerator.generateAccessToken(userId, roles)
    }

    companion object {
        private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        private const val authCodeLength = 6
    }
}
