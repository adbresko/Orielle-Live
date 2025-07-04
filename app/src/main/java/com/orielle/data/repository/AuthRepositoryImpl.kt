package com.orielle.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.orielle.domain.model.Response
import com.orielle.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    override fun signUpWithEmailAndPassword(displayName: String, email: String, password: String): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val newUser = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email,
                            displayName = displayName
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

    override fun signInWithGoogle(idToken: String): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // After successful sign-in, check if user exists in Firestore.
                    // If not, create their profile.
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val userRef = db.collection("users").document(firebaseUser.uid)
                        userRef.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                // New user, create their profile in Firestore
                                val newUser = User(
                                    uid = firebaseUser.uid,
                                    email = firebaseUser.email,
                                    displayName = firebaseUser.displayName
                                )
                                addUserToFirestore(newUser) // Re-use our existing function
                            }
                            // Whether new or existing, the sign-in was successful.
                            trySend(Response.Success(true))
                        }.addOnFailureListener { e ->
                            trySend(Response.Failure(e))
                        }
                    } else {
                        trySend(Response.Failure(Exception("Google Sign-In failed.")))
                    }
                } else {
                    trySend(Response.Failure(task.exception ?: Exception("Unknown Google Sign-In error.")))
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
