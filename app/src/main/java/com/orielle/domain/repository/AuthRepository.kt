package com.orielle.domain.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseUser
import com.orielle.domain.model.Response
import com.orielle.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Creates a new user with email and password.
     * On success, it now returns the full FirebaseUser object.
     */
    fun signUpWithEmail(email: String, firstName: String, password: String): Flow<Response<FirebaseUser>>

    /**
     * Signs in a user with their email and password.
     */
    fun signInWithEmail(email: String, password: String): Flow<Response<Boolean>>

    /**
     * Signs in a user using a Google ID token.
     */
    fun signInWithGoogle(idToken: String): Flow<Response<Boolean>>

    /**
     * Signs in a user using an Apple ID token.
     */
    fun signInWithApple(idToken: String): Flow<Response<Boolean>>

    /**
     * Creates or updates a user's profile information in Firestore.
     */
    fun addUserToFirestore(user: User): Flow<Response<Boolean>>
}