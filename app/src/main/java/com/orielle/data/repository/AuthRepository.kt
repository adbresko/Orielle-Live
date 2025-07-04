package com.orielle.data.repository

import com.orielle.domain.model.Response
import com.orielle.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun signUpWithEmailAndPassword(displayName: String, email: String, password: String): Flow<Response<Boolean>>

    fun signInWithEmailAndPassword(email: String, password: String): Flow<Response<Boolean>>

    /**
     * Signs in a user using a Google ID token and creates their profile in Firestore if they are a new user.
     * @param idToken The ID token received from the Google Sign-In flow.
     * @return A Flow that emits the authentication response.
     */
    fun signInWithGoogle(idToken: String): Flow<Response<Boolean>>

    /**
     * Signs in a user using an Apple ID token and creates their profile in Firestore if they are a new user.
     * @param idToken The ID token received from the Apple Sign-In flow.
     * @return A Flow that emits the authentication response.
     */
    fun signInWithApple(idToken: String): Flow<com.orielle.domain.model.Response<Boolean>>

    fun addUserToFirestore(user: User): Flow<Response<Boolean>>
}
