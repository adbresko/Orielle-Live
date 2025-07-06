package com.orielle.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.orielle.domain.model.EmailAlreadyInUseException
import com.orielle.domain.model.Response
import com.orielle.domain.model.User
import com.orielle.domain.model.WeakPasswordException
import com.orielle.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    override fun signUpWithEmail(email: String, password: String): Flow<Response<FirebaseUser>> = callbackFlow {
        trySend(Response.Loading)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val newUser = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email,
                            displayName = null,
                            hasAgreedToTerms = true
                        )
                        db.collection("users").document(newUser.uid).set(newUser)
                            .addOnSuccessListener {
                                // On success, we now return the entire FirebaseUser object.
                                trySend(Response.Success(firebaseUser))
                            }
                            .addOnFailureListener { firestoreException ->
                                trySend(Response.Failure(firestoreException))
                            }
                    } else {
                        trySend(Response.Failure(Exception("User creation failed.")))
                    }
                } else {
                    val e = task.exception
                    when (e) {
                        is FirebaseAuthWeakPasswordException -> trySend(Response.Failure(WeakPasswordException()))
                        is FirebaseAuthUserCollisionException -> trySend(Response.Failure(EmailAlreadyInUseException()))
                        else -> trySend(Response.Failure(e ?: Exception("Unknown sign-up error.")))
                    }
                }
            }
        awaitClose { channel.close() }
    }

    override fun signInWithEmail(email: String, password: String): Flow<Response<Boolean>> {
        // ... this function remains unchanged ...
        return callbackFlow { /* ... */ }
    }

    override fun signInWithGoogle(idToken: String): Flow<Response<Boolean>> {
        // ... this function remains unchanged ...
        return callbackFlow { /* ... */ }
    }

    override fun signInWithApple(idToken: String): Flow<Response<Boolean>> {
        // ... this function remains unchanged ...
        return callbackFlow { /* ... */ }
    }

    override fun addUserToFirestore(user: User): Flow<Response<Boolean>> {
        // ... this function remains unchanged ...
        return callbackFlow { /* ... */ }
    }
}