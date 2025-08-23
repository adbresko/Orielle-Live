package com.orielle.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.orielle.R
import com.orielle.data.manager.CacheManager
import com.orielle.data.manager.EnhancedSyncManager
import com.orielle.data.manager.SyncResult
import com.orielle.data.manager.DataConflict
import com.orielle.domain.model.UserActivityLevel
import com.orielle.domain.manager.SessionManager
import com.orielle.util.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.Result

/**
 * Smart auto-sync service that intelligently manages background synchronization
 */
@AndroidEntryPoint
class SmartAutoSyncService : LifecycleService() {

    @Inject
    lateinit var enhancedSyncManager: EnhancedSyncManager

    @Inject
    lateinit var cacheManager: CacheManager

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var networkUtils: NetworkUtils

    private val serviceScope = lifecycleScope
    private var syncJob: Job? = null
    private var networkMonitorJob: Job? = null
    private var userActivityJob: Job? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "smart_sync_channel"
        private const val SYNC_INTERVAL_ACTIVE = 5 * 60 * 1000L      // 5 minutes for active users
        private const val SYNC_INTERVAL_INACTIVE = 15 * 60 * 1000L   // 15 minutes for inactive users
        private const val SYNC_INTERVAL_BACKGROUND = 60 * 60 * 1000L // 1 hour for background
        private const val MAX_SYNC_DURATION = 2 * 60 * 1000L         // 2 minutes max sync time

        const val ACTION_START_SYNC = "com.orielle.START_SMART_SYNC"
        const val ACTION_STOP_SYNC = "com.orielle.STOP_SMART_SYNC"
        const val ACTION_FORCE_SYNC = "com.orielle.FORCE_SYNC"
        const val ACTION_UPDATE_USER_ACTIVITY = "com.orielle.UPDATE_USER_ACTIVITY"

        fun startService(context: Context) {
            val intent = Intent(context, SmartAutoSyncService::class.java).apply {
                action = ACTION_START_SYNC
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SmartAutoSyncService::class.java).apply {
                action = ACTION_STOP_SYNC
            }
            context.startService(intent)
        }

        fun forceSync(context: Context) {
            val intent = Intent(context, SmartAutoSyncService::class.java).apply {
                action = ACTION_FORCE_SYNC
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("🚀 Smart Auto-Sync Service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Smart sync active"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("📱 Smart Auto-Sync Service started")

        when (intent?.action) {
            ACTION_START_SYNC -> startSmartSync()
            ACTION_STOP_SYNC -> stopSmartSync()
            ACTION_FORCE_SYNC -> forceSync()
            ACTION_UPDATE_USER_ACTIVITY -> updateUserActivity()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("🛑 Smart Auto-Sync Service destroyed")
        stopAllJobs()
    }

    /**
     * Start intelligent background synchronization
     */
    private fun startSmartSync() {
        if (syncJob?.isActive == true) {
            Timber.d("🔄 Sync already running, skipping start request")
            return
        }

        Timber.d("🚀 Starting smart sync service")

        // Start network monitoring
        startNetworkMonitoring()

        // Start user activity monitoring
        startUserActivityMonitoring()

        // Start periodic sync
        startPeriodicSync()

        updateNotification("Smart sync running")
    }

    /**
     * Stop all synchronization
     */
    private fun stopSmartSync() {
        Timber.d("⏹️ Stopping smart sync service")
        stopAllJobs()
        updateNotification("Smart sync stopped")
    }

    /**
     * Force immediate synchronization
     */
    private fun forceSync() {
        Timber.d("⚡ Force sync requested")

        syncJob?.cancel()
        syncJob = serviceScope.launch {
            try {
                withTimeout(MAX_SYNC_DURATION) {
                    enhancedSyncManager.syncWithConflictResolution()
                    Timber.d("✅ Force sync completed successfully")
                }
            } catch (e: TimeoutCancellationException) {
                Timber.w("⏰ Force sync timed out")
            } catch (e: Exception) {
                Timber.e(e, "❌ Force sync failed")
            }
        }
    }

    /**
     * Start network connectivity monitoring
     */
    private fun startNetworkMonitoring() {
        networkMonitorJob?.cancel()
        networkMonitorJob = serviceScope.launch {
            networkUtils.observeNetworkConnectivity()
                .distinctUntilChanged()
                .collect { isConnected ->
                    if (isConnected) {
                        Timber.d("🌐 Network connected, triggering sync")
                        triggerSyncOnNetworkChange()
                    } else {
                        Timber.d("📡 Network disconnected, pausing sync")
                        pauseSync()
                    }
                }
        }
    }

    /**
     * Start user activity monitoring
     */
    private fun startUserActivityMonitoring() {
        userActivityJob?.cancel()
        userActivityJob = serviceScope.launch {
            while (isActive) {
                try {
                    cacheManager.updateUserActivity()
                    delay(5 * 60 * 1000L) // Update every 5 minutes
                } catch (e: Exception) {
                    Timber.w(e, "Failed to update user activity")
                }
            }
        }
    }

    /**
     * Start periodic synchronization based on user activity
     */
    private fun startPeriodicSync() {
        syncJob?.cancel()
        syncJob = serviceScope.launch {
            while (isActive) {
                try {
                    val userActivity = getUserActivityLevel()
                    val syncInterval = getSyncInterval(userActivity)

                    Timber.d("🔄 Periodic sync scheduled in ${syncInterval / 1000} seconds for $userActivity user")

                    delay(syncInterval)

                    if (isActive && networkUtils.isNetworkAvailable()) {
                        performSmartSync()
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error in periodic sync loop")
                    delay(60 * 1000L) // Wait 1 minute on error
                }
            }
        }
    }

    /**
     * Perform intelligent synchronization
     */
    private suspend fun performSmartSync() {
        if (enhancedSyncManager.isSyncing.value) {
            Timber.d("🔄 Sync already in progress, skipping")
            return
        }

        try {
            Timber.d("🔄 Starting smart sync")
            updateNotification("Syncing data...")

            withTimeout(MAX_SYNC_DURATION) {
                val result = enhancedSyncManager.syncWithConflictResolution()

                when {
                    result.isSuccess -> {
                        val syncResult = result.getOrNull()
                        when (syncResult) {
                            is SyncResult.Success -> {
                                Timber.d("✅ Smart sync completed successfully, resolved ${syncResult.conflictsResolved} conflicts")
                                updateNotification("Sync completed (${syncResult.conflictsResolved} conflicts resolved)")
                            }
                            is SyncResult.NeedsUserInput -> {
                                Timber.w("⚠️ Sync needs user input for ${syncResult.conflicts.size} conflicts")
                                updateNotification("Sync needs attention (${syncResult.conflicts.size} conflicts)")
                                // TODO: Show notification to user about conflicts
                            }
                            is SyncResult.Failure -> {
                                Timber.e("❌ Smart sync failed: ${syncResult.error}")
                                updateNotification("Sync failed: ${syncResult.error}")
                            }
                            else -> {
                                Timber.w("⚠️ Unknown sync result type")
                                updateNotification("Sync completed with unknown result")
                            }
                        }
                    }
                    result.isFailure -> {
                        Timber.e("❌ Smart sync failed: ${result.exceptionOrNull()?.message}")
                        updateNotification("Sync failed: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w("⏰ Smart sync timed out")
            updateNotification("Sync timed out")
        } catch (e: Exception) {
            Timber.e(e, "❌ Smart sync failed")
            updateNotification("Sync failed")
        }
    }

    /**
     * Trigger sync when network becomes available
     */
    private suspend fun triggerSyncOnNetworkChange() {
        delay(2000) // Wait 2 seconds for network to stabilize

        if (networkUtils.isNetworkAvailable()) {
            Timber.d("🌐 Network stable, triggering sync")
            performSmartSync()
        }
    }

    /**
     * Pause synchronization when network is unavailable
     */
    private suspend fun pauseSync() {
        Timber.d("📡 Pausing sync due to network unavailability")
        // Sync will resume when network monitoring detects reconnection
    }

    /**
     * Get sync interval based on user activity
     */
    private fun getSyncInterval(userActivity: UserActivityLevel): Long {
        return when (userActivity) {
            UserActivityLevel.ACTIVE -> SYNC_INTERVAL_ACTIVE
            UserActivityLevel.INACTIVE -> SYNC_INTERVAL_INACTIVE
            UserActivityLevel.BACKGROUND -> SYNC_INTERVAL_BACKGROUND
        }
    }

    /**
     * Get current user activity level
     */
    private suspend fun getUserActivityLevel(): UserActivityLevel {
        // This would be implemented in CacheManager
        return UserActivityLevel.ACTIVE // Default for now
    }

    /**
     * Update user activity
     */
    private fun updateUserActivity() {
        serviceScope.launch {
            try {
                cacheManager.updateUserActivity()
                Timber.d("👤 User activity updated")
            } catch (e: Exception) {
                Timber.w(e, "Failed to update user activity")
            }
        }
    }

    /**
     * Stop all background jobs
     */
    private fun stopAllJobs() {
        syncJob?.cancel()
        networkMonitorJob?.cancel()
        userActivityJob?.cancel()

        syncJob = null
        networkMonitorJob = null
        userActivityJob = null
    }

    /**
     * Create notification channel for Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Smart Sync Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Manages background data synchronization"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create notification
     */
    private fun createNotification(content: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Orielle Smart Sync")
        .setContentText(content)
        .setSmallIcon(R.drawable.ic_orielle_drop)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    /**
     * Update notification content
     */
    private fun updateNotification(content: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(content))
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

}
