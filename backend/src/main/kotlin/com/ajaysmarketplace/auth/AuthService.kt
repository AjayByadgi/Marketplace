package com.ajaysmarketplace.auth

import com.ajaysmarketplace.user.User
import com.ajaysmarketplace.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(request: RegisterRequest): TokenPair {
        if (userRepository.existsByEmail(request.email.lowercase().trim())) {
            throw IllegalArgumentException("Email already registered")
        }

        val user = userRepository.save(
            User(
                email = request.email.lowercase().trim(),
                password = passwordEncoder.encode(request.password)!!,
                firstName = request.firstName.trim(),
                lastName = request.lastName.trim()
            )
        )

        return buildTokenPair(user)
    }

    fun login(request: LoginRequest): TokenPair {
        val user = userRepository.findByEmail(request.email.lowercase().trim())
            ?: throw IllegalArgumentException("Invalid credentials")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        return buildTokenPair(user)
    }

    fun refresh(refreshToken: String): TokenPair {
        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val userId = jwtService.extractUserId(refreshToken)
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        return buildTokenPair(user)
    }

    fun buildUserResponse(user: User) = AuthResponse(
        email = user.email,
        firstName = user.firstName,
        lastName = user.lastName,
        role = user.role
    )

    private fun buildTokenPair(user: User): TokenPair {
        val userId = user.id!!
        return TokenPair(
            accessToken = jwtService.generateAccessToken(userId, user.email, user.role),
            refreshToken = jwtService.generateRefreshToken(userId),
            user = user
        )
    }
}

// Internal data class — never leaves the service layer
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)