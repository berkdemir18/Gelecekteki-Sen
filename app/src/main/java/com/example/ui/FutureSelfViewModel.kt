package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.GeminiRepository
import com.example.data.Goal
import com.example.data.JournalEntry
import com.example.data.MoodMetric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FutureSelfViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val journalDao = db.journalDao()
    private val chatDao = db.chatDao()
    private val goalDao = db.goalDao()
    private val moodMetricDao = db.moodMetricDao()

    // Flow for journal entries
    val journalEntries: StateFlow<List<JournalEntry>> = journalDao.getAllEntriesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Flow for mood metrics
    val moodMetrics: StateFlow<List<MoodMetric>> = moodMetricDao.getAllMetricsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Flow for chat history
    val chatMessages: StateFlow<List<ChatMessage>> = chatDao.getAllMessagesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Flow for goals
    val goals: StateFlow<List<Goal>> = goalDao.getAllGoalsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _letterFromFuture = MutableStateFlow<String?>(null)
    val letterFromFuture: StateFlow<String?> = _letterFromFuture.asStateFlow()

    private val _isLetterLoading = MutableStateFlow(false)
    val isLetterLoading: StateFlow<Boolean> = _isLetterLoading.asStateFlow()

    init {
        // Pre-populate system greeting if conversation is completely empty
        viewModelScope.launch {
            val count = chatDao.getRecentMessagesDesc().size
            if (count == 0) {
                chatDao.insertMessage(
                    ChatMessage(
                        sender = "future_self",
                        message = "Merhaba güzel kalpli halim! Ben senin bundan tam 5 yıl sonraki versiyonunum. Yaşadığın acıları, hissettiğin kaygıları ve içindeki o eşsiz potansiyeli dün gibi hatırlıyorum. Bugün burada olmamın sebebi sana o yollardan başarıyla geçtiğimizi söylemek, her düştüğünde elinden tutmak ve sana gücünü hatırlatmak.\n\nBugün nasılsın? Bana hissettiğin her şeyi çekinmeden yazabilir, günlüğünden detaylar paylaşabilirsin. Ben her zaman yanındayım.",
                        timestamp = System.currentTimeMillis() - 1000
                    )
                )
            }
        }
    }

    // --- Journal Operations ---
    fun addJournalEntry(content: String, mood: String, wins: String, struggles: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(Date())
            val entry = JournalEntry(
                date = dateStr,
                mood = mood,
                content = content,
                wins = wins,
                struggles = struggles
            )
            journalDao.insertEntry(entry)

            // Auto send a notification message in chat from future self acknowledging today's journal
            val ackMessage = "Sevgili kendim, bugün günlük yazdığını hissettim. Günlüğüne '$content' yazıp kendini ifade etmen çok asil bir davranış. Ruh halini '$mood' olarak seçmişsin. İnan bana, şu an içinden geçtiğin bu evre, gelecekteki beni o kadar olgunlaştırıyor ki! Ne zaman istersen yukarıdaki 'Sohbet' sekmesinden bu konuyu benimle daha derin konuşabilirsin. Seni her halinle kucaklıyorum."
            chatDao.insertMessage(
                ChatMessage(
                    sender = "future_self",
                    message = ackMessage,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteJournalEntry(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            journalDao.deleteEntryById(id)
        }
    }

    // --- Chat Operations ---
    fun sendChatMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return

        viewModelScope.launch {
            // 1. Save user message to DB
            val userMsg = ChatMessage(sender = "user", message = messageText)
            withContext(Dispatchers.IO) {
                chatDao.insertMessage(userMsg)
            }

            _isChatLoading.value = true

            // 2. Fetch recent data context
            val recentJournals = withContext(Dispatchers.IO) {
                journalDao.getRecentEntries()
            }
            val chatHistory = withContext(Dispatchers.IO) {
                // Ensure we retrieve existing messages including the newly inserted user message
                chatDao.getRecentMessagesDesc().reversed()
            }

            // 3. Make the API Call or get fallback
            val replyText = withContext(Dispatchers.Default) {
                GeminiRepository.getFutureSelfResponse(
                    userMessage = messageText,
                    recentJournals = recentJournals,
                    chatHistory = chatHistory
                )
            }

            // Parse secret emotional metrics block from Gemini response
            val jsonRegex = "(?s)```json\\s*(.*?)\\s*```".toRegex()
            val matchResult = jsonRegex.find(replyText)
            var parsedScore = 7
            var parsedEmotion = "hope"
            var parsedHabit = "self_dialogue"

            if (matchResult != null) {
                val jsonString = matchResult.groupValues[1]
                val parsed = parseMoodMetric(jsonString)
                if (parsed != null) {
                    parsedScore = parsed.first
                    parsedEmotion = parsed.second
                    parsedHabit = parsed.third
                }
            } else {
                // If offline fallback is generated, guess based on some keywords in Turkish
                val lower = replyText.lowercase()
                when {
                    lower.contains("endişe") || lower.contains("kork") -> {
                        parsedScore = 4
                        parsedEmotion = "anxiety"
                        parsedHabit = "grounding"
                    }
                    lower.contains("stres") || lower.contains("yorgun") -> {
                        parsedScore = 5
                        parsedEmotion = "stress"
                        parsedHabit = "breathing"
                    }
                    lower.contains("mutlu") || lower.contains("harika") || lower.contains("başar") -> {
                        parsedScore = 9
                        parsedEmotion = "joy"
                        parsedHabit = "celebration"
                    }
                }
            }

            // Save metrics to DB
            withContext(Dispatchers.IO) {
                moodMetricDao.insertMetric(
                    MoodMetric(
                        score = parsedScore,
                        emotion = parsedEmotion,
                        habit = parsedHabit
                    )
                )
            }

            // Extract cleaner response without JSON metadata for chat display
            val cleanReplyText = replyText.replace(jsonRegex, "").trim()

            // 4. Save reply message to DB
            val replyMsg = ChatMessage(sender = "future_self", message = cleanReplyText)
            withContext(Dispatchers.IO) {
                chatDao.insertMessage(replyMsg)
            }

            _isChatLoading.value = false
        }
    }

    private fun parseMoodMetric(rawJson: String): Triple<Int, String, String>? {
        return try {
            val moodScoreRegex = "\"mood_score\"\\s*:\\s*(\\d+)".toRegex()
            val detectedEmotionRegex = "\"detected_emotion\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val habitTriggeredRegex = "\"habit_triggered\"\\s*:\\s*\"([^\"]+)\"".toRegex()

            val score = moodScoreRegex.find(rawJson)?.groupValues?.get(1)?.toIntOrNull() ?: 7
            val emotion = detectedEmotionRegex.find(rawJson)?.groupValues?.get(1) ?: "hope"
            val habit = habitTriggeredRegex.find(rawJson)?.groupValues?.get(1) ?: "self_dialogue"

            Triple(score, emotion, habit)
        } catch (e: Exception) {
            null
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.clearHistory()
            // Add initial welcome back
            chatDao.insertMessage(
                ChatMessage(
                    sender = "future_self",
                    message = "Hafızamızı sıfırladın ama kalbimizin bağı baki! Ben senin her zaman 5 yıl sonradan elini tutmaya hazır halinim. Hadi, bugün aklından geçenleri benimle baştan paylaş."
                )
            )
        }
    }

    // --- Goal Operations ---
    fun addGoal(title: String, targetYear: Int, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newGoal = Goal(title = title, targetYear = targetYear, category = category)
            goalDao.insertGoal(newGoal)

            // Let future self send an encouraging comment in the chat
            val categoryTurkish = when(category) {
                "Kariyer" -> "kariyer"
                "Sağlık" -> "sağlık ve spor"
                "İlişkiler" -> "insani ilişkilerimiz"
                "Zihinsel" -> "zihinsel gelişimimiz"
                else -> "gelecek planlarımız"
            }
            val targetYearLabel = "$targetYear. Yıl"
            val responseText = "İnanılmazsın! Az önce hedefler hanene yeni bir tohum ektin: '$title' ($targetYearLabel Hedefi, Kategori: $category). 5 yıl sonradan sana fısıldayayım: Bu hedef üzerine attığımız o samimi adımlar o kadar güzel meyve verdi ki! Gelecekte bunu başarmış olmamızın gururunu şu an yaşıyorum. Şimdi, bugün bu yolda atmak istediğin ilk minicik adım ne olacak?"
            
            chatDao.insertMessage(
                ChatMessage(
                    sender = "future_self",
                    message = responseText,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleGoalAchievement(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = goal.copy(isAchieved = !goal.isAchieved)
            goalDao.updateGoal(updated)

            if (updated.isAchieved) {
                val congo = "Harika! Görev tamamlandı: '${goal.title}'. Bu engeli de aştık! Sana söylemiştim değil mi, sen her şeyin üstesinden gelebilecek bir güce sahipsin. 5 yıl sonraki ben olarak seninle gurur duyuyorum, canım kendim!"
                chatDao.insertMessage(ChatMessage(sender = "future_self", message = congo))
            }
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteGoalById(id)
        }
    }

    // --- Insights (Letter Generation) ---
    fun generateLetterFromFuture() {
        viewModelScope.launch {
            _isLetterLoading.value = true
            val recentJournals = withContext(Dispatchers.IO) {
                journalDao.getRecentEntries()
            }
            
            val queryText = "Lütfen benim geçmişteki günlüklerimi, ruh hallerimi analiz ederek bana gelecekten o özel sevgini ve şefkatini gösteren derin, uzun, samimi bir mektup yaz. Bu mektup sadece benim için olsun ve doğrudan kalbime dokunsun."
            val replyText = withContext(Dispatchers.Default) {
                GeminiRepository.getFutureSelfResponse(
                    userMessage = queryText,
                    recentJournals = recentJournals,
                    chatHistory = emptyList()
                )
            }
            val jsonRegex = "(?s)```json\\s*(.*?)\\s*```".toRegex()
            val cleanLetter = replyText.replace(jsonRegex, "").trim()
            _letterFromFuture.value = cleanLetter
            _isLetterLoading.value = false
        }
    }
}
