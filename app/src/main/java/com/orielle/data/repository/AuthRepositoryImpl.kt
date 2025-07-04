package com.orielle.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.orielle.domain.model.Response
import com.orielle.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    override fun signUpWithEmailAndPassword(
        displayName: String,
        email: String,
        password: String
    ): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val newUser = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email,
                            displayName = displayName,
                            hasAgreedToTerms = true // Set terms agreement on sign-up
                        )
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

    override fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<Response<Boolean>> = callbackFlow {
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

    override fun signInWithGoogle(idToken: String): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val userRef = db.collection("users").document(firebaseUser.uid)
                        userRef.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                // New user via Google, create their profile
                                val newUser = User(
                                    uid = firebaseUser.uid,
                                    email = firebaseUser.email,
                                    displayName = firebaseUser.displayName,
                                    hasAgreedToTerms = true // Set terms agreement on social sign-up
                                )
                                addUserToFirestore(newUser)
                            }
                            trySend(Response.Success(true))
                        }.addOnFailureListener { e ->
                            trySend(Response.Failure(e))
                        }
                    } else {
                        trySend(Response.Failure(Exception("Google Sign-In failed.")))
                    }
                } else {
                    trySend(
                        Response.Failure(
                            task.exception ?: Exception("Unknown Google Sign-In error.")
                        )
                    )
                }
            }
        awaitClose { channel.close() }
    }

    override fun signInWithApple(idToken: String): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading)
        val credential = OAuthProvider.newCredentialBuilder("apple.com")
            .setIdToken(idToken)
            .build()
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val userRef = db.collection("users").document(firebaseUser.uid)
                        userRef.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                // New user via Apple, create their profile
                                val newUser = User(
                                    uid = firebaseUser.uid,
                                    email = firebaseUser.email,
                                    displayName = firebaseUser.displayName,
                                    hasAgreedToTerms = true // Set terms agreement on social sign-up
                                )
                                addUserToFirestore(newUser)
                            }
                            trySend(Response.Success(true))
                        }.addOnFailureListener { e ->
                            trySend(Response.Failure(e))
                        }
                    } else {
                        trySend(Response.Failure(Exception("Apple Sign-In failed.")))
                    }
                } else {
                    trySend(
                        Response.Failure(
                            task.exception ?: Exception("Unknown Apple Sign-In error.")
                        )
                    )
                }
            }
        awaitClose { channel.close() }
    }

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
