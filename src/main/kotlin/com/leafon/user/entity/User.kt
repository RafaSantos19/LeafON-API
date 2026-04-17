package com.leafon.user.entity

import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Generated

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Column
import jakarta.persistence.Id
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.UUID


@Entity
@Table(name = "users")
class User(
    @Id
    @Generated
    @ColumnDefault("gen_random_uuid()")
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = true)
    var name: String? = null,

    @CreationTimestamp
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    var updatedAt: OffsetDateTime? = null,
)
