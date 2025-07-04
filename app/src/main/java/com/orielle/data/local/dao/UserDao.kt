// path: app/src/main/java/com/orielle/data/local/dao/UserDao.kt
package com.orielle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orielle.data.local.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user WHERE uid = :uid")
    fun getUser(uid: String): Flow<UserEntity?>

    @Query("DELETE FROM user")
    suspend fun clearUser()
}