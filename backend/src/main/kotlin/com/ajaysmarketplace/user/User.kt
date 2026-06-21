package com.ajaysmarketplace.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime
import java.util.UUID


@Table("users")
// tells spring that this class maps to the users table in postgres

// Kotlin data class auto-generates equals(), hashCode(), toString(), and copy() — you get all of that for free.
// Every entity and DTO should be a data class.
data class User(
    @Id
    val id: UUID? = null,
    // @Id = primary key. Nullable because when we CREATE a new user
    // we don't have an id yet — postgres generates it for us.
    // After save() it comes back populated.

    @Column("email")
    val email: String,

    val password: String,

    @Column("first_name")
    val firstName: String,

    @Column("last_name")
    val lastName: String,

    val role: String = "ROLE_USER",

    @Column("created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column("updated_at")
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),

    )

