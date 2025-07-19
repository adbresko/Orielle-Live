package com.orielle.data.mapper

import com.orielle.data.local.model.UserEntity
import com.orielle.domain.model.User

/**
 * Mapper class to convert between UserEntity (data layer) and User (domain layer).
 */
object UserMapper {

    /**
     * Converts a UserEntity to a User domain model.
     */
    fun UserEntity.toDomain(): User {
        return User(
            uid = this.uid,
            email = this.email,
            displayName = this.displayName,
            firstName = this.firstName,
            lastName = this.lastName,
            hasAgreedToTerms = this.hasAgreedToTerms,
            createdAt = this.createdAt,
            lastLoginAt = this.lastLoginAt,
            isPremium = this.isPremium
        )
    }

    /**
     * Converts a User domain model to a UserEntity.
     */
    fun User.toEntity(): UserEntity {
        return UserEntity(
            uid = this.uid,
            email = this.email,
            displayName = this.displayName,
            firstName = this.firstName,
            lastName = this.lastName,
            hasAgreedToTerms = this.hasAgreedToTerms,
            createdAt = this.createdAt,
            lastLoginAt = this.lastLoginAt,
            isPremium = this.isPremium
        )
    }
}