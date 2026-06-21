package com.ajaysmarketplace.auth

// Claims: The "Payload" (JSON data) inside the JWT (e.g., email, role)
import io.jsonwebtoken.Claims
// Jwts: The main entry point for the library to build/parse tokens
import io.jsonwebtoken.Jwts
// Keys: Utility to turn a raw string into a cryptographic key
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service // Tells Spring: "Manage this class as a singleton bean. I want to use it elsewhere."
class JwtService(
    // @Value: Injects values from application.properties or .env.
    // This keeps your code clean and separates secret keys from your logic.
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-expiry}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry}") private val refreshTokenExpiry: Long
) {

    // 'by lazy': This is a performance optimization.
    // It prevents the computer from converting the secret string to a Key
    // until the very first time we actually need to sign or verify a token.
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    // --- GENERATION ---

    // A JWT is essentially: A Header (algorithm) + A Payload (data) + A Signature.
    // This function creates the payload for an Access Token.
    fun generateAccessToken(userId: UUID, email: String, role: String): String =
        buildToken(
            subject = userId.toString(), // The 'sub' field in JWT (The User ID)
            expiry = accessTokenExpiry,
            extraClaims = mapOf(
                "email" to email,
                "role" to role,
                "type" to "access"   // We add a tag so we know this isn't a refresh token
            )
        )

    fun generateRefreshToken(userId: UUID): String =
        buildToken(
            subject = userId.toString(),
            expiry = refreshTokenExpiry,
            extraClaims = mapOf("type" to "refresh")  // A 'refresh' tag
        )

    // --- VALIDATION ---

    // runCatching: The "Safe" way to handle things that might crash.
    // If the token is fake, expired, or tampered with, parseSignedClaims will THROW an error.
    // runCatching catches the error and returns a Result object; getOrDefault(false)
    // makes sure we just get 'false' back instead of the whole server crashing.
    fun isTokenValid(token: String): Boolean = runCatching {
        // We parse it, and check the expiration date against 'now' (Date())
        extractAllClaims(token).expiration.after(Date())
    }.getOrDefault(false)

    // Checks if the 'type' claim is 'refresh' so we don't accidentally
    // treat a long-lived refresh token as a short-lived access token.
    fun isRefreshToken(token: String): Boolean =
        extractClaim(token) { it["type"] as? String } == "refresh"

    // --- EXTRACTION ---
    // These functions "peel the onion" of the JWT to get specific data out.

    fun extractUserId(token: String): UUID =
        UUID.fromString(extractClaim(token) { it.subject })

    fun extractEmail(token: String): String =
        extractClaim(token) { it["email"] as String }

    fun extractRole(token: String): String =
        extractClaim(token) { it["role"] as String }

    // --- INTERNAL HELPERS ---

    // The core builder: This performs the actual math to "sign" the token.
    private fun buildToken(
        subject: String,
        expiry: Long,
        extraClaims: Map<String, Any>
    ): String = Jwts.builder()
        .subject(subject)
        .claims(extraClaims)
        .issuedAt(Date()) // Timestamp: When was this made?
        .expiration(Date(System.currentTimeMillis() + expiry)) // When does it die?
        .signWith(signingKey) // The secret "wax seal" that proves the server made it
        .compact()   // The final assembly into the "xxxxx.yyyyy.zzzzz" string format

    // This decodes the token.
    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey as SecretKey) // Check the wax seal
            .build()
            .parseSignedClaims(token) // If seal is broken (tampered), this throws error
            .payload // Return the JSON data inside

    // Higher-Order Function:
    // This is a "template." It says: "Run this extraction logic on the token,
    // and let the caller decide which specific piece of data (the 'resolver') they want."
    private fun <T> extractClaim(token: String, resolver: (Claims) -> T): T =
        resolver(extractAllClaims(token))
}

//Phase 1: The Login (Creating the Wax Seal)User sends Email + Password.Server checks DB. If valid, Server creates a payload: {"userId": "123", "role": "admin"}.
// Server takes Payload + JWT_SECRET and performs a mathematical calculation (hashing). This results in a unique Signature.
// Server combines them: Header.Payload.Signature $\rightarrow$ The Token.Server sets this as an HttpOnly cookie.
//
// Phase 2: Accessing a Protected Page (Verifying the Seal)Client sends the request.
// The browser automatically attaches the HttpOnly cookie.Server receives the request.
// It splits the token into its three parts.Server grabs the Payload from the cookie and your JWT_SECRET from its own environment variables.
// Server does the math: Hash(Payload + Secret).The Comparison:If the result equals the Signature attached to the token,
// the server says: "I generated this. It has not been changed. This user is valid."If a hacker changed the role from user to admin inside the payload,
// the Hash would come out completely different. The server sees the mismatch and rejects the request immediately.