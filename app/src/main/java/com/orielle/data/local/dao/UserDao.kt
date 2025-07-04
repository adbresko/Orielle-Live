package com.orielle.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.orielle.data.local.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertUser(user: UserEntity)

    /**
     * Corrected query to use the 'users' table (plural).
     */
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUser(uid: String): Flow<UserEntity?>

    /**
     * Corrected query to use the 'users' table (plural).
     */
    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUser(uid: String)
}
