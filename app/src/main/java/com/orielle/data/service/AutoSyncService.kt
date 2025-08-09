package com.orielle.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.orielle.domain.manager.SyncManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Background service that automatically syncs data when network becomes available.
 * This ensures offline-first behavior with automatic sync on connectivity restore.
 */
@AndroidEntryPoint
class AutoSyncService : Service() {

    @Inject
    lateinit var syncManager: SyncManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var networkObserverJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        startNetworkObserver()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        networkObserverJob?.cancel()
    }

    private fun startNetworkObserver() {
        networkObserverJob = serviceScope.launch {
            var wasOnline = false

            syncManager.isNetworkAvailable.collect { isOnline ->
                Timber.d("Network state changed: $isOnline")

                // If we just came back online, trigger sync
                if (isOnline && !wasOnline) {
                    Timber.i("Network restored, triggering automatic sync...")

                    syncManager.syncPendingData().fold(
                        onSuccess = {
                            Timber.i("Automatic sync completed successfully")
                        },
                        onFailure = { error ->
                            Timber.w(error, "Automatic sync failed")
                        }
                    )
                }

                wasOnline = isOnline
            }
        }
    }

    companion object {
        const val ACTION_START_AUTO_SYNC = "com.orielle.START_AUTO_SYNC"
        const val ACTION_STOP_AUTO_SYNC = "com.orielle.STOP_AUTO_SYNC"
    }
}
