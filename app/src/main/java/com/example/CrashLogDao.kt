package com.example

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CrashLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crashLog: CrashLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(crashLog: CrashLog): Long

    @Query("SELECT * FROM crash_logs ORDER BY timestamp DESC")
    fun getAllCrashLogs(): Flow<List<CrashLog>>

    @Query("DELETE FROM crash_logs")
    suspend fun clearCrashLogs()
}
