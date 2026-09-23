package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY createdAt DESC")
    fun getAllRecordings(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentRecordings(limit: Int = 4): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE id = :id")
    fun getRecordingById(id: Long): Flow<RecordingEntity?>

    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun getRecordingByIdSync(id: Long): RecordingEntity?

    @Query("SELECT * FROM recordings WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchRecordings(query: String): Flow<List<RecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recordings: List<RecordingEntity>)

    @Update
    suspend fun updateRecording(recording: RecordingEntity)

    @Query("UPDATE recordings SET title = :newTitle WHERE id = :id")
    suspend fun renameRecording(id: Long, newTitle: String)

    @Query("UPDATE recordings SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Delete
    suspend fun deleteRecording(recording: RecordingEntity)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recordings WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM recordings")
    suspend fun getCount(): Int
}
