package com.pppp0722.nulllovebe.global.config

import com.auth0.jwt.JWTVerifier
import com.fasterxml.jackson.databind.ObjectMapper
import com.pppp0722.nulllovebe.global.exception.ErrorCode
import com.pppp0722.nulllovebe.global.exception.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.CorsUtils
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val customCorsConfigurationSource: CustomCorsConfigurationSource,
    private val accessTokenAuthenticationFilter: AccessTokenAuthenticationFilter,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 인증/인가
            .authorizeRequests()
            .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
            .antMatchers(
                "/users/send-auth-code", "/users/sign-up", "/users/login", "/users/reissue-access-token"
            ,"/api/save", "/api/read"
            ).permitAll()
            .anyRequest().hasAnyRole("USER")
            .and()
            // 사용하지 않는 기능 비활성화
            .formLogin()
            .disable()
            .csrf()
            .disable()
            .headers()
            .disable()
            .httpBasic()
            .disable()
            .rememberMe()
            .disable()
            .logout()
            .disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // JWT
            .addFilterBefore(
                accessTokenAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            // 인증/인가 실패
            .exceptionHandling()
            .accessDeniedHandler(customAccessDeniedHandler)
            .and()
            // CORS
            .cors().configurationSource(customCorsConfigurationSource)
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}

@Component
class CustomCorsConfigurationSource : CorsConfigurationSource {
    override fun getCorsConfiguration(request: HttpServletRequest): CorsConfiguration {
        return CorsConfiguration().apply {
            allowCredentials = true
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "PATCH", "DELETE")
            allowedHeaders = listOf("*")
        }
    }
}

@Component
class AccessTokenAuthenticationFilter(
    private val jwtVerifier: JWTVerifier
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = request.getHeader("Access-Token")

        if (accessToken.isNullOrEmpty()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val decodedAccessToken = jwtVerifier.verify(accessToken)

            val roles = decodedAccessToken.getClaim("roles").asList(String::class.java)
                .map(::SimpleGrantedAuthority)

            val authentication =
                UsernamePasswordAuthenticationToken(decodedAccessToken.audience[0], null, roles)

            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: Exception) {
            log.warn("Access Token 검증 실패", e)
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AccessTokenAuthenticationFilter::class.java)
    }
}

@Component
class CustomAccessDeniedHandler : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        val errorCode = ErrorCode.ACCESS_DINIED
        response.status = errorCode.httpStatus.value()
        response.contentType = "text/plain;charset=UTF-8"

        val objectMapper = ObjectMapper()
        val json = objectMapper.writeValueAsString(ErrorResponse(errorCode.message))
        response.writer.write(json)
        response.writer.flush()
        response.writer.close()

        log.warn("허가되지 않은 요청, remoteAddr: {}", request.remoteAddr)
    }

    companion object {
        private val log = LoggerFactory.getLogger(CustomAccessDeniedHandler::class.java)
    }
}