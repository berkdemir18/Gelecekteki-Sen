package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntriesFlow(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC LIMIT 5")
    suspend fun getRecentEntries(): List<JournalEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Int)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 15")
    suspend fun getRecentMessagesDesc(): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM user_goals ORDER BY targetYear ASC, timestamp DESC")
    fun getAllGoalsFlow(): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Query("DELETE FROM user_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Int)
}

@Dao
interface MoodMetricDao {
    @Query("SELECT * FROM mood_metrics ORDER BY timestamp ASC")
    fun getAllMetricsFlow(): Flow<List<MoodMetric>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: MoodMetric)

    @Query("DELETE FROM mood_metrics")
    suspend fun clearMetrics()
}

