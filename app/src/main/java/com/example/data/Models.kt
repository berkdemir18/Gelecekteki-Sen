package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val mood: String, // e.g., "Mutlu", "Stresli", "Endişeli", "Huzurlu", "Sıradan"
    val content: String,
    val wins: String = "",
    val struggles: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "future_self"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetYear: Int, // 1 to 5 representing years from now (e.g. 1. Yıl, 3. Yıl, 5. Yıl)
    val category: String, // "Kariyer", "Maddi", "İlişkiler", "Sağlık", "Zihinsel"
    val isAchieved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mood_metrics")
data class MoodMetric(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int, // 1 to 10
    val emotion: String, // e.g., "Joy", "Anxiety", "Sadness", "Hope"
    val habit: String,
    val timestamp: Long = System.currentTimeMillis()
)
