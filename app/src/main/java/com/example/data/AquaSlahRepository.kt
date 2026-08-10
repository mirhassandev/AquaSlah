package com.example.data

import com.example.data.dao.HydrationDao
import com.example.data.dao.JournalDao
import com.example.data.dao.PrayerDao
import com.example.data.dao.UserDao
import com.example.data.models.HydrationLog
import com.example.data.models.JournalEntry
import com.example.data.models.PrayerLog
import com.example.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AquaSlahRepository(
    private val hydrationDao: HydrationDao,
    private val prayerDao: PrayerDao,
    private val journalDao: JournalDao,
    private val userDao: UserDao
) {
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    val allHydrationLogs: Flow<List<HydrationLog>> = hydrationDao.getAllLogs()
    
    val todayHydrationLogs: Flow<List<HydrationLog>> = hydrationDao.getLogsForDate(getTodayDateString())

    val todayHydrationTotalMl: Flow<Int> = hydrationDao.getTotalMlForDate(getTodayDateString()).map { it ?: 0 }

    val allPrayerLogs: Flow<List<PrayerLog>> = prayerDao.getAllLogs()

    val todayPrayerLogs: Flow<List<PrayerLog>> = prayerDao.getLogsForDate(getTodayDateString())

    val allJournalEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()

    val userProfile: Flow<UserProfile?> = userDao.getUserProfile()

    suspend fun addHydrationLog(amountMl: Int) {
        val dateStr = getTodayDateString()
        hydrationDao.insertLog(
            HydrationLog(
                amountMl = amountMl,
                timestamp = System.currentTimeMillis(),
                dateString = dateStr
            )
        )
    }

    suspend fun deleteHydrationLog(id: Long) {
        hydrationDao.deleteLogById(id)
    }

    suspend fun logPrayerStatus(prayerName: String, status: String) {
        val dateStr = getTodayDateString()
        val existing = prayerDao.getLogForPrayer(dateStr, prayerName)
        val logToSave = existing?.copy(status = status, timestamp = System.currentTimeMillis())
            ?: PrayerLog(
                prayerName = prayerName,
                status = status,
                timestamp = System.currentTimeMillis(),
                dateString = dateStr
            )
        prayerDao.insertOrUpdateLog(logToSave)
    }

    suspend fun addJournalEntry(title: String, reflection: String, associatedPrayer: String?, tags: String) {
        journalDao.insertEntry(
            JournalEntry(
                title = title,
                reflection = reflection,
                associatedPrayer = associatedPrayer,
                tags = tags,
                timestamp = System.currentTimeMillis(),
                dateString = getTodayDateString()
            )
        )
    }

    suspend fun deleteJournalEntry(id: Long) {
        journalDao.deleteEntryById(id)
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userDao.saveUserProfile(profile)
    }

    suspend fun initDefaultProfileIfNeeded() {
        val profile = userDao.getUserProfileDirect()
        if (profile == null) {
            userDao.saveUserProfile(UserProfile())
        }
    }
}
