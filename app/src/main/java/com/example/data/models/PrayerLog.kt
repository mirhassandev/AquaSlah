package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_logs")
data class PrayerLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prayerName: String, // Fajr, Dhuhr, Asr, Maghrib, Isha, Tahajjud, Duha
    val status: String, // PRAYED, MISSED, QADA
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // YYYY-MM-DD
)
