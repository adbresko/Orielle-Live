package com.orielle.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orielle.domain.model.AppError
import com.orielle.domain.model.EmailAlreadyInUseException
import com.orielle.domain.model.Response
import com.orielle.domain.model.User
import com.orielle.domain.model.WeakPasswordException
import com.orielle.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import timber.log.Timber

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
                                trySend(Response.Success(firebaseUser))
                            }
                            .addOnFailureListener { firestoreException ->
                                Timber.e(firestoreException, "Firestore error during sign up")
                                FirebaseCrashlytics.getInstance().recordException(firestoreException)
                                trySend(Response.Failure(AppError.Database, firestoreException))
                            }
                    } else {
                        val ex = Exception("User creation failed.")
                        Timber.e(ex)
                        FirebaseCrashlytics.getInstance().recordException(ex)
                        trySend(Response.Failure(AppError.Unknown, ex))
                    }
                } else {
                    val e = task.exception
                    when (e) {
                        is FirebaseAuthWeakPasswordException -> trySend(Response.Failure(AppError.Auth, WeakPasswordException()))
                        is FirebaseAuthUserCollisionException -> trySend(Response.Failure(AppError.Auth, EmailAlreadyInUseException()))
                        else -> {
                            Timber.e(e, "Unknown sign up error")
                            FirebaseCrashlytics.getInstance().recordException(e ?: Exception("Unknown sign-up error."))
                            trySend(Response.Failure(AppError.Unknown, e ?: Exception("Unknown sign-up error.")))
                        }
                    }
                }
            }
        awaitClose { channel.close() }
    }

    override fun signInWithEmail(email: String, password: String): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Response.Success(true))
                } else {
                    val e = task.exception
                    Timber.e(e, "Sign in error")
                    FirebaseCrashlytics.getInstance().recordException(e ?: Exception("Sign-in failed."))
                    trySend(Response.Failure(AppError.Auth, e ?: Exception("Sign-in failed.")))
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
                        db.collection("users").document(firebaseUser.uid).get()
                            .addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    val newUser = User(
                                        uid = firebaseUser.uid,
                                        email = firebaseUser.email,
                                        displayName = firebaseUser.displayName,
                                        hasAgreedToTerms = true
                                    )
                                    db.collection("users").document(newUser.uid).set(newUser)
                                        .addOnSuccessListener {
                                            trySend(Response.Success(true))
                                        }
                                        .addOnFailureListener { e ->
                                            Timber.e(e, "Firestore error during Google sign in")
                                            FirebaseCrashlytics.getInstance().recordException(e)
                                            trySend(Response.Failure(AppError.Database, e))
                                        }
                                } else {
                                    trySend(Response.Success(true))
                                }
                            }
                            .addOnFailureListener { e ->
                                Timber.e(e, "Firestore error during Google sign in")
                                FirebaseCrashlytics.getInstance().recordException(e)
                                trySend(Response.Failure(AppError.Database, e))
                            }
                    } else {
                        trySend(Response.Success(true))
                    }
                } else {
                    val e = task.exception
                    Timber.e(e, "Google sign in error")
                    FirebaseCrashlytics.getInstance().recordException(e ?: Exception("Google sign-in failed."))
                    trySend(Response.Failure(AppError.Auth, e ?: Exception("Google sign-in failed.")))
                }
            }
        awaitClose { channel.close() }
    }

    override fun signInWithApple(idToken: String): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading)
        // For Apple Sign-In with Firebase, we need to create a credential from the ID token
        // This approach doesn't require an Activity parameter
        try {
            // Create a credential from the Apple ID token
            val credential = OAuthProvider.newCredentialBuilder("apple.com")
                .setIdToken(idToken)
                .build()

            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        if (firebaseUser != null) {
                            db.collection("users").document(firebaseUser.uid).get()
                                .addOnSuccessListener { document ->
                                    if (!document.exists()) {
                                        val newUser = User(
                                            uid = firebaseUser.uid,
                                            email = firebaseUser.email,
                                            displayName = firebaseUser.displayName,
                                            hasAgreedToTerms = true
                                        )
                                        db.collection("users").document(newUser.uid).set(newUser)
                                            .addOnSuccessListener {
                                                trySend(Response.Success(true))
                                            }
                                            .addOnFailureListener { e ->
                                                Timber.e(e, "Firestore error during Apple sign in")
                                                FirebaseCrashlytics.getInstance().recordException(e)
                                                trySend(Response.Failure(AppError.Database, e))
                                            }
                                    } else {
                                        trySend(Response.Success(true))
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Timber.e(e, "Firestore error during Apple sign in")
                                    FirebaseCrashlytics.getInstance().recordException(e)
                                    trySend(Response.Failure(AppError.Database, e))
                                }
                        } else {
                            trySend(Response.Success(true))
                        }
                    } else {
                        val e = task.exception
                        Timber.e(e, "Apple sign in error")
                        FirebaseCrashlytics.getInstance().recordException(e ?: Exception("Apple sign-in failed."))
                        trySend(Response.Failure(AppError.Auth, e ?: Exception("Apple sign-in failed.")))
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Apple sign in credential creation error")
            FirebaseCrashlytics.getInstance().recordException(e)
            trySend(Response.Failure(AppError.Auth, e))
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
                Timber.e(e, "Firestore error during addUserToFirestore")
                FirebaseCrashlytics.getInstance().recordException(e)
                trySend(Response.Failure(AppError.Database, e))
            }
        awaitClose { channel.close() }
    }
}