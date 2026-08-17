package com.example

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClipboardItem): Long

    @Query("SELECT * FROM clipboard_history ORDER BY isPinned DESC, timestamp DESC")
    fun getAll(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_history ORDER BY isPinned DESC, timestamp DESC LIMIT :limit")
    fun getWithLimit(limit: Int): Flow<List<ClipboardItem>>

    @Query("DELETE FROM clipboard_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE clipboard_history SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("DELETE FROM clipboard_history WHERE isPinned = 0")
    suspend fun clearUnpinned()

    @Query("DELETE FROM clipboard_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM clipboard_history")
    suspend fun getCount(): Int

    @Query("DELETE FROM clipboard_history WHERE id NOT IN (SELECT id FROM clipboard_history ORDER BY isPinned DESC, timestamp DESC LIMIT :limit)")
    suspend fun trimHistory(limit: Int)
}
