package com.orielle.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * An interface for the Authentication Repository.
 * This defines the contract for all authentication-related data operations.
 */
interface AuthRepository {

    /**
     * Creates a new user with the given email and password.
     * @return A Flow that emits the authentication response.
     */
    fun signUpWithEmailAndPassword(email: String, password: String): Flow<com.orielle.domain.model.Response<Boolean>>

    /**
     * Signs in a user with the given email and password.
     * @return A Flow that emits the authentication response.
     */
    fun signInWithEmailAndPassword(email: String, password: String): Flow<com.orielle.domain.model.Response<Boolean>>

    /**
     * Adds a new user's profile to the remote database (Firestore).
     * @return A Flow that emits the result of the operation.
     */
    fun addUserToFirestore(user: com.orielle.domain.model.User): Flow<com.orielle.domain.model.Response<Boolean>>
}
