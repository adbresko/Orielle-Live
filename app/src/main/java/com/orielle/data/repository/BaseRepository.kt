package com.orielle.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.manager.SyncManager
import com.orielle.domain.model.AppError
import com.orielle.domain.model.Response
import com.orielle.util.NetworkUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Base repository with common offline-first patterns and Firebase sync logic.
 */
abstract class BaseRepository(
    protected val firestore: FirebaseFirestore,
    protected val sessionManager: SessionManager,
    protected val syncManager: SyncManager? = null
) {

    /**
     * Execute Firebase operation with offline-first error handling.
     * If the operation fails due to network issues, it will be queued for later sync.
     */
    protected suspend fun <T> executeFirebaseOperation(
        operation: suspend () -> T,
        localFallback: suspend () -> T
    ): Response<T> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))

            val isGuest = sessionManager.isGuest.first()

            if (isGuest) {
                // Guest users only use local storage
                Response.Success(localFallback())
            } else {
                // Try Firebase operation first
                try {
                    val result = operation()
                    Response.Success(result)
                } catch (networkError: Exception) {
                    Timber.w(networkError, "Firebase operation failed, falling back to local storage")

                    // Log for crash analytics but don't crash the app
                    FirebaseCrashlytics.getInstance().recordException(networkError)

                    // Use local fallback and mark for later sync
                    val localResult = localFallback()

                    // Schedule sync when network is available (if sync manager is available)
                    syncManager?.let { manager ->
                        if (manager.isCurrentlyOnline()) {
                            // Network is available but Firebase operation failed, retry once
                            try {
                                operation()
                            } catch (retryError: Exception) {
                                Timber.w(retryError, "Firebase retry also failed")
                                // Will be synced later by AutoSyncService
                            }
                        }
                    }

                    Response.Success(localResult)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Repository operation failed")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    /**
     * Save data with offline-first approach
     */
    protected suspend fun <T> saveWithOfflineSupport(
        data: T,
        localSave: suspend (T) -> Unit,
        firebaseSave: suspend (T, String) -> Unit,
        getDataId: (T) -> String
    ): Response<String> {
        return executeFirebaseOperation(
            operation = {
                val userId = sessionManager.currentUserId.first()!!
                localSave(data)
                firebaseSave(data, userId)
                getDataId(data)
            },
            localFallback = {
                localSave(data)
                getDataId(data)
            }
        )
    }

    /**
     * Delete data with offline-first approach
     */
    protected suspend fun deleteWithOfflineSupport(
        itemId: String,
        localDelete: suspend (String) -> Unit,
        firebaseDelete: suspend (String, String) -> Unit
    ): Response<Unit> {
        return executeFirebaseOperation(
            operation = {
                val userId = sessionManager.currentUserId.first()!!
                localDelete(itemId)
                firebaseDelete(itemId, userId)
            },
            localFallback = {
                localDelete(itemId)
            }
        )
    }
}
