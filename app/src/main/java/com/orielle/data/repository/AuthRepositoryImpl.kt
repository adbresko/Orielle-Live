package com.orielle.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.orielle.domain.model.Response
import com.orielle.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * The concrete implementation of the AuthRepository.
 * This class handles all the logic for interacting with Firebase Authentication and Firestore.
 *
 * @param auth An instance of FirebaseAuth, provided by Hilt.
 * @param db An instance of FirebaseFirestore, provided by Hilt.
 */
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    override fun signUpWithEmailAndPassword(email: String, password: String): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // After successful sign-up, add the user to Firestore
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val newUser = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email,
                            displayName = firebaseUser.email?.substringBefore('@') // A sensible default
                        )
                        // We will handle the result of this nested call if needed,
                        // for now we assume it succeeds if the auth task succeeds.
                        db.collection("users").document(newUser.uid).set(newUser)
                            .addOnSuccessListener {
                                trySend(Response.Success(true))
                            }
                            .addOnFailureListener { e ->
                                trySend(Response.Failure(e))
                            }
                    } else {
                        trySend(Response.Failure(Exception("User creation failed.")))
                    }
                } else {
                    trySend(Response.Failure(task.exception ?: Exception("Unknown sign-up error.")))
                }
            }
        awaitClose { channel.close() }
    }

    override fun signInWithEmailAndPassword(email: String, password: String): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Response.Success(true))
                } else {
                    trySend(Response.Failure(task.exception ?: Exception("Unknown sign-in error.")))
                }
            }
        awaitClose { channel.close() }
    }

    // This function is now handled within the signUp flow, but is kept for potential future use.
    override fun addUserToFirestore(user: User): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading)
        db.collection("users").document(user.uid).set(user)
            .addOnSuccessListener {
                trySend(Response.Success(true))
            }
            .addOnFailureListener { e ->
                trySend(Response.Failure(e))
            }
        awaitClose { channel.close() }
    }
}
