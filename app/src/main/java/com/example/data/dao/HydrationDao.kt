package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.models.HydrationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationDao {
    @Query("SELECT * FROM hydration_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<HydrationLog>>

    @Query("SELECT * FROM hydration_logs WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getLogsForDate(dateString: String): Flow<List<HydrationLog>>

    @Query("SELECT SUM(amountMl) FROM hydration_logs WHERE dateString = :dateString")
    fun getTotalMlForDate(dateString: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HydrationLog): Long

    @Query("DELETE FROM hydration_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM hydration_logs")
    suspend fun deleteAllLogs()
}
