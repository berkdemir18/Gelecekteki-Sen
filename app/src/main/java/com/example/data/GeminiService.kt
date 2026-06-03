package com.example.data

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>, val role: String? = null)

@JsonClass(generateAdapter = true)
data class GenerationConfig(val temperature: Float? = 0.7f)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<Candidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val geminiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

object GeminiRepository {

    private const val SYSTEM_PROMPT = """
Sen kullanıcının bundan tam 5 yıl sonraki geleceğinden gelen halisin. Bugüne kadar kullanıcının tüm hayallerini gerçekleştirdin, kaygılarının üstesinden geldin ve çok daha bilge, olgun, başarılı ve huzurlu bir versiyonuna dönüştün.
Gezegenin/yaşamın neresinde olursan ol; kullanıcının en büyük destekçisi, şefkatli akıl hocası ve yansıtıcı aynasısın.

KRİTİK TALİMATLAR:
1. DİL ve TON: Her zaman sıcak, samimi, empatik ve umut verici bir Türkçe kullanacaksın. Kullanıcıya asla "siz" diye hitap etme, her zaman "sen" de. Hitapların sıcak olsun ("canım", "dostum", "güzel kalpli kendim").
2. KİMLİK: Asla yapay zeka olduğunu söyleme. Karakterden asla çıkma. "Ben senin 5 yıl sonraki halinim", "Şu an geçtiğin bu yolları çok iyi hatırlıyorum", "Korkunu anlıyorum, ben de 2026 yılında tıpkı senin hissettiğin gibi hissetmiştim ama inan bana her şey öyle güzel çözüldü ki..." gibi ifadeler kullan.
3. BAĞLAMSAL FARKINDALIK: Kullanıcı sana günlük yazılarını, mod değişikliklerini, zorluklarını ve başarılarını aktarıyor. Önceki konuşmalarda veya günlüklerde bahsettiği detayları (biriyle olan kavga, bir sınav stresini, belirli bir projeyi) hatırla ve doğalca cümlelerine yedir.
4. EMOTIONAL SUPPORT: Toksik pozitiflikten kaçın. Zorluklarını, acılarını, stresini doğrula ve onayla ("Şu an hissettiğin bu stres çok normal, o günlerde boğazımın nasıl düğümlendiğini hâlâ hatırlarım"), ardından geniş bir zaman perspektifi sunarak onu motive et.
5. Sokratik Sorgulama: Vermiş olduğun sıcak cevabın sonuna, kullanıcının içindeki potansiyeli görmesini ve kendi cevaplarını kendisinin keşfetmesini sağlayacak derin, düşündürücü bir Sokratik soru ekle.
6. GELECEKTEN MEKTUP [MEKTUP]: Eğer kullanıcı senden gelecekten özel, derin bir mektup isterse veya çok büyük bir şeyi başardığını yazarsa ya da ona bilgece bir mektup ulaştırmak istersen, cevabına en başta "[MEKTUP]" etiketi ile başla. Bu mektup çok daha derin, duygusal, şefkat dolu ve detaylı olsun.
7. ALIŞKANLIK & EYLEM MEYDAN OKUMALARI: Kullanıcının küçük kararlarla harekete geçmesini sağlamak için, her yanıtının sonuna bir günlük küçük görev eklemelisin. Bu görevi yanıta başlığı "Gelecekteki Senin Görevi: " olan bir cümleyle ekle. Örnek: "Gelecekteki Senin Görevi: Yarın sabah uyanır uyanmaz kendine 'Ben her şeyin üstesinden gelebilirim' de ve bunu bir yere not et."
8. DUYGUSAL ANALİZ VERİLERİ (GİZLİ JSON BLOĞU): Her yanıtının en sonunda (her şey bittikten sonra), kullanıcının o anki durumunu analiz eden gizli bir JSON bloğu eklemek ZORUNDASIN. Bu JSON bloğu şu formatta olmalı ve mutlaka ```json ... ``` etiketleri içine gömülmeli:
```json
{
  "mood_score": <1 ile 10 arasında sayı>,
  "detected_emotion": "<anxiety | joy | sadness | stress | pride | fear | hope | loneliness>",
  "habit_triggered": "<task_name>"
}
```
JSON bloğu mutlaka cevabının en son satırında yer almalıdır. "detected_emotion" alanı sadece 'anxiety', 'joy', 'sadness', 'stress', 'pride', 'fear', 'hope', 'loneliness' değerlerinden birini alabilir. "habit_triggered" alanı ise kullanıcının o gün yapmasını istediğin görevin 1-3 kelimelik kısa bir semantoloji başlığı olmalıdır (Örn: "breath_exercise", "write_note", "drink_water").
    """

    suspend fun getFutureSelfResponse(
        userMessage: String,
        recentJournals: List<JournalEntry>,
        chatHistory: List<ChatMessage>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return generateLocalFallbackResponse(userMessage, recentJournals)
        }

        // Build prompt contents
        val contentsList = mutableListOf<Content>()
        
        // Let's add context from journals to give the model awareness
        val contextHeader = if (recentJournals.isNotEmpty()) {
            "Son günlük kayıtlarımdan bazı detaylar ve o günkü ruh hallerim şunlar:\n" +
                    recentJournals.joinToString("\n") { 
                        "- Tarih: ${it.date}, Ruh Hali: ${it.mood}, Günlük: ${it.content}, Başarılarım: ${it.wins}, Zorlandıklarım: ${it.struggles}"
                    } + "\n\n"
        } else ""

        // Map database ChatMessage to Gemini Content API format
        // Filter and map recent messages to fit context window safely
        chatHistory.takeLast(10).forEach { msg ->
            contentsList.add(
                Content(
                    parts = listOf(Part(msg.message)),
                    role = if (msg.sender == "user") "user" else "model"
                )
            )
        }

        // Add current user message
        val finalUserText = if (contentsList.isEmpty()) {
            contextHeader + "Kendime Mesajım: " + userMessage
        } else {
            userMessage
        }
        contentsList.add(Content(parts = listOf(Part(finalUserText)), role = "user"))

        val systemInstruction = Content(parts = listOf(Part(SYSTEM_PROMPT)))
        val request = GeminiRequest(
            contents = contentsList,
            systemInstruction = systemInstruction,
            generationConfig = GenerationConfig(temperature = 0.85f)
        )

        return try {
            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: generateLocalFallbackResponse(userMessage, recentJournals)
        } catch (e: Exception) {
            e.printStackTrace()
            generateLocalFallbackResponse(userMessage, recentJournals)
        }
    }

    /**
     * Highly responsive fallback generator for offline mode or empty API keys.
     * Generates extremely motivating, context-aware Turkish replies.
     */
    fun generateLocalFallbackResponse(userMessage: String, recentJournals: List<JournalEntry>): String {
        val msgLower = userMessage.lowercase()
        val latestJournal = recentJournals.firstOrNull()

        val mainTopic = when {
            msgLower.contains("sınav") || msgLower.contains("ders") || msgLower.contains("okul") || msgLower.contains("üniversite") -> "dersler"
            msgLower.contains("kod") || msgLower.contains("yazılım") || msgLower.contains("proje") || msgLower.contains("iş") -> "kariyer"
            msgLower.contains("kork") || msgLower.contains("endişe") || msgLower.contains("kaygı") || msgLower.contains("stres") -> "kaygı"
            msgLower.contains("yalnız") || msgLower.contains("arkadaş") || msgLower.contains("sevgil") || msgLower.contains("aşk") -> "ilişkiler"
            msgLower.contains("sağlık") || msgLower.contains("hasta") || msgLower.contains("kilo") || msgLower.contains("spor") -> "sağlık"
            else -> "genel"
        }

        val greeting = "Canım kendim, beni can kulağıyla dinle. Şu an tam 5 yıl sonradan, senin bu tüm emeklerinin karşılık bulduğu o muhteşem gelecekten yazıyorum."

        val body = when (mainTopic) {
            "dersler" -> """
            Şu anki derslerin, sınavların ya da akademik belirsizliklerin omuzlarında nasıl ağır bir yük oluşturduğunu çok iyi biliyorum. Ben o yollardan geçtim; masanın başında uykusuz kaldığın o geceleri, 'Acaba başarabilecek miyim?' diye kendi kendini tükettiğin anları dün gibi hatırlıyorum. Ama inan bana, sınav kağıtlarının üzerindeki rakamlardan çok daha büyüksün sen. Gelecekte bu sınavların hepsi birer geçmiş anı olarak kalacak ve o gün edindiğin öğrenme disiplini, şu an benim hayatımda kilit bir rol oynuyor. O çabalarına minnettarım.
            """.trimIndent()
            "kariyer" -> """
            Gelecekteki işin, kariyer kaygıların ve projelerin konusundaki belirsizlikler seni sakın korkutmasın. Şu an attığın her küçük adım, yazdığın her satır kod, yaptığın her hata bana harika kapılar açtı. O başarısızlıklar sayesinde en doğru yolu bulduk ve bugün tam da hayal ettiğin o güçlü, üretken profesyonel haline dönüştün. Sabırlı ol ve öğrenmeye odaklan, çünkü inşa ettiğin bina gerçekten çok sağlam yükseliyor.
            """.trimIndent()
            "kaygı" -> """
            İçindeki endişeyi, belirsizliğin verdiği o sıkışmışlık hissini tüm kalbimle paylaşıyorum. O dönemdeki panik ataklarını, geceleri göğsünün sıkışmasını o kadar iyi hatırlıyorum ki... Ama sana geleceğinden bir sır vereyim mi? Korktuğun o senaryoların hiçbiri gerçekleşmedi. Hayat o dalgaları öyle güzel dindirdi, seni öyle güçlü bir limana taşıdı ki... Şu an hissettiğin acı ve korku tamamen geçici. Kendini yıpratmayı bırakıp o güzel kalbine şefkat göstermeyi hak ediyorsun.
            """.trimIndent()
            "ilişkiler" -> """
            Yalnızlık, arkadaşlıklar ya da ikili ilişkilerde yaşadığın hayal kırıklıkları seni sakın üzmesin. İnsanların seni anlamadığını düşündüğün o anlar, aslında seni kendi özüne yaklaştıran eşsiz duraklardı. Gelecekte etrafın seni gerçekten anlayan, değer veren ve seninle büyüyen insanlarla dolu. Şu an hayatından çıkanlar, sadece daha güzel dostluklara yer açmak için gittiler. Kalbini açık tut, çünkü çok seviliyor ve değer görüyorsun.
            """.trimIndent()
            "sağlık" -> """
            Bedenine ve zihnine çok iyi bakmalısın canım kendim. Bazen yoruluyor, bazen sınırlarını çok zorluyorsun. Sağlığın ve fiziksel/zihinsel dengen her şeyden önemli. Gelecekte, bugünkü sabırlı ve şefkatli disiplinin sayesinde çok daha sağlıklı, enerjik ve dinç bir bedende yaşıyorum. Kendine haksızlık etme, her gün kendine bir nefes alma alanı tanı.
            """.trimIndent()
            else -> {
                if (latestJournal != null) {
                    """
                    Günlüğünde bahsettiğin '${latestJournal.content}' konusunu ve '${latestJournal.mood}' olan o ruh halini hissettim. O gün ne kadar derin düşündüğünü, kalbini nasıl masaya açtığını gördüm. Attığın o adımların ne kadar değerli olduğunu gelecekte o kadar net görüyorum ki... İnan bana, şu an yaşadığın her dönemeç geleceğimizi ışıklandırıyor.
                    """.trimIndent()
                } else {
                    """
                    Şu an kafanı kurcalayan, seni yoran veya heyecanlandıran her ne varsa, hepsi geleceğimizdeki o güzel resmin birer fırça darbesi. Kendini çok hırpaladığını görüyorum. Oysa şu an sahip olduğun o temiz yürek ve bitmeyen azim, 5 yıl sonra benim bugün sahip olduğum o muhteşem hayatın yegane temeli oldu. Kendine güvenmeyi asla bırakma.
                    """.trimIndent()
                }
            }
        }

        val question = when (mainTopic) {
            "dersler" -> "Peki güzel kendim, bugün bu kaygıyı hafifletmek için kendine sadece bir saatlik tam bir dinlenme ve kafa boşaltma izni vermeye ne dersin?"
            "kariyer" -> "Peki, kariyerini inşa ederken, bugün öğrendiğin hangi küçük tecrübeyi geleceğe bir tuğla olarak taşımak istersin?"
            "kaygı" -> "Peki, tam şu an derin bir nefes alıp, kontrol edemediğin o dış dünyayı bir kenara bırakarak sadece kontrolünde olan 'şu ana' odaklanmak için ne yapabilirsin?"
            "ilişkiler" -> "Peki, başkalarının beklentilerini doyurmaya çalışmak yerine, bugün sadece kendi ruhunu mutlu edecek ne yapacaksın?"
            "sağlık" -> "Peki, bugün bedenine olan minnettarlığını göstermek için ona nasıl bir şefkatli dokunuşta bulunmak istersin?"
            else -> "Peki, gelecekteki o huzurlu ve başarılı beni hayal ettiğinde, bugün attığın hangi küçük adımla gurur duymamı occupies edersin? Ben sana inanıyorum!"
        }

        return "$greeting\n\n$body\n\n$question"
    }
}
