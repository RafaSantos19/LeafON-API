package com.leafon.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(unique = true)
    var email: String? = null,

    @Column(nullable = true)
    var name: String? = null,

    @Column(nullable = true)
    var phone: String? = null,

    @CreationTimestamp
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    var updatedAt: OffsetDateTime? = null,
)
