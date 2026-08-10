package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val reflection: String,
    val associatedPrayer: String? = null,
    val tags: String = "Gratitude", // Comma-separated: Gratitude,Dua,Reflection,Goals
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String
)
