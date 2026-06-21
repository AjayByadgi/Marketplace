package com.ajaysmarketplace.auth

// what the client needs to register
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

// what the client needs to login
data class LoginRequest(
    val email: String,
    val password: String
)

// what the client needs to get a new access token
data class RefreshRequest(
    val refreshToken: String
)

// what we send back to the client after succesful login/register
data class AuthResponse(
    val email: String,
    val role: String,
    val firstName: String,
    val lastName: String
)
//Why do we need these instead of just using User?
// Three reasons. First, your User entity has the hashed password — you never want that in an HTTP response.
// Second, your entity has createdAt, updatedAt, internal fields the client doesn't need.
// Third, DTOs let you change your database schema without changing your API contract and vice versa.