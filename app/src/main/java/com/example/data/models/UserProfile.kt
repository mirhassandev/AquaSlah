package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Muslim Seeker",
    val email: String = "user@aquaslah.app",
    val dailyHydrationGoalMl: Int = 2500,
    val calculationMethod: String = "ISNA", // ISNA, MWL, UmmAlQura, Egyptian, Karachi, Tehran
    val asrMadhab: String = "Standard", // Standard, Hanafi
    val cityOverride: String = "Mecca",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val isBiometricsEnabled: Boolean = false,
    val isDarkMode: Boolean = true,
    val language: String = "EN", // EN, AR, UR, FR, ID
    val hydrationReminderIntervalMinutes: Int = 120,
    val isCloudSynced: Boolean = true,
    val hijriDayOffset: Int = 0
)
