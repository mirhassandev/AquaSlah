package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.models.PrayerLog
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<PrayerLog>>

    @Query("SELECT * FROM prayer_logs WHERE dateString = :dateString")
    fun getLogsForDate(dateString: String): Flow<List<PrayerLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: PrayerLog): Long

    @Query("SELECT * FROM prayer_logs WHERE dateString = :dateString AND prayerName = :prayerName LIMIT 1")
    suspend fun getLogForPrayer(dateString: String, prayerName: String): PrayerLog?

    @Query("DELETE FROM prayer_logs")
    suspend fun deleteAllLogs()
}
