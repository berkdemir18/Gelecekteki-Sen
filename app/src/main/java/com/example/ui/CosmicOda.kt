package com.example.ui

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*

// Solfeggio / Sound healing frequencies object
object CosmicFrequencyEngine {
    private var audioTrack: AudioTrack? = null
    private var playJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    val activeFrequency = mutableStateOf<Double?>(null)

    fun toggleFrequency(freq: Double) {
        if (activeFrequency.value == freq) {
            stop()
        } else {
            stop()
            activeFrequency.value = freq
            playJob = scope.launch {
                try {
                    val sampleRate = 22050
                    val bufferSize = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    
                    val track = AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM
                    ).also { audioTrack = it }
                    
                    track.play()
                    
                    val samples = ShortArray(bufferSize)
                    var angle = 0.0
                    val factor = 2 * Math.PI * freq / sampleRate
                    
                    while (isActive) {
                        for (i in samples.indices) {
                            // Sinusoidal wave scaling to about 25% height for calm, warm hums
                            samples[i] = (Math.sin(angle) * 7500).toInt().toShort()
                            angle += factor
                            if (angle > 2 * Math.PI) {
                                angle -= 2 * Math.PI
                            }
                        }
                        track.write(samples, 0, samples.size)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        audioTrack?.stop()
                        audioTrack?.release()
                    } catch (e: Exception) {}
                    audioTrack = null
                }
            }
        }
    }

    fun stop() {
        activeFrequency.value = null
        playJob?.cancel()
        playJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {}
        audioTrack = null
    }
}

// Data structures for the cards and frequencies
data class CosmicWhisper(
    val id: Int,
    val message: String,
    val source: String,
    val icon: String,
    val cardGradientColors: List<Color>
)

data class FrequencyItem(
    val freq: Double,
    val title: String,
    val slogan: String,
    val benefit: String,
    val emoji: String,
    val color: Color
)

@Composable
fun CosmicOdaScreen() {
    var sectionTab by remember { mutableStateOf("Nefes") } // Nefes, Fısıltı, Frekans
    
    // Automatically stop frequency when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            CosmicFrequencyEngine.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Applet Header with detailed futuristic typography
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Kozmik Oda 🌌",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Geleceğinden gelen motivasyon ve farkındalık alanı",
                    fontSize = 11.sp,
                    color = MoonlightGray
                )
            }
        }

        // Section tab row selectors with premium dynamic selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpaceOnyx, RoundedCornerShape(14.dp))
                .border(BorderStroke(1.dp, NebulaDeep), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Triple("Nefes", "Nefes Balonu", Icons.Default.FilterCenterFocus),
                Triple("Fısıltı", "Kozmik Fısıltı", Icons.Default.AutoAwesome),
                Triple("Frekans", "Şifa Frekansı", Icons.Default.LibraryMusic)
            )

            tabs.forEach { (id, label, icon) ->
                val isSelected = sectionTab == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) NebulaDeep else Color.Transparent)
                        .clickable { sectionTab = id }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) CelestialAmber else MoonlightGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else MoonlightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render selected section with smooth AnimatedContent transition
        AnimatedContent(
            targetState = sectionTab,
            transitionSpec = {
                slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
            },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            label = "ZenPortalSections"
        ) { selection ->
            when (selection) {
                "Nefes" -> CosmicBreathingSection()
                "Fısıltı" -> CosmicWisdomCardSection()
                "Frekans" -> CosmicFrequencySection()
            }
        }
    }
}

// ============================================
// SECTION 1: DYNAMIC BREATHING EXERCISE BUBBLE
// ============================================
@Composable
fun CosmicBreathingSection() {
    var breathePhase by remember { mutableStateOf("Başla") } // Başla, Nefes Al, Tut, Nefes Ver
    var secondsRemaining by remember { mutableStateOf(4) }
    var scaleValue by remember { mutableStateOf(0.6f) }
    val coroutineScope = rememberCoroutineScope()
    var runningJob by remember { mutableStateOf<Job?>(null) }
    
    // Smooth custom scaling state
    val animateScale by animateFloatAsState(
        targetValue = scaleValue,
        animationSpec = tween(durationMillis = if (breathePhase == "Tut") 50 else 3800, easing = LinearEasing),
        label = "BreatheScale"
    )

    // Animated glow colors corresponding to each breathing stage
    val glowColor = when (breathePhase) {
        "Nefes Al" -> CalmTeal
        "Tut" -> GoldenSun
        "Nefes Ver" -> SoftPink
        else -> CelestialAmber
    }

    DisposableEffect(Unit) {
        onDispose {
            runningJob?.cancel()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(SpaceOnyx)
            .border(BorderStroke(1.dp, NebulaDeep), RoundedCornerShape(24.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = "Zaman Balonu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Zihnini dindirip, 5 yıl sonranın enerjisini hissetmek için benimle birlikte senkronize nefes al.",
                    fontSize = 11.sp,
                    color = MoonlightGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Interactive Breathing Circle Display
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Outer slow-spinning ripple glow
                Spacer(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(animateScale + 0.15f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(glowColor.copy(alpha = 0.18f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )

                // The Golden breathing bubble itself
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(animateScale)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            ambientColor = glowColor,
                            spotColor = glowColor
                        )
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(glowColor, glowColor.copy(alpha = 0.7f))
                            ),
                            CircleShape
                        )
                        .border(3.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (breathePhase == "Başla") "💤" else secondsRemaining.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SpaceOnyx
                        )
                    }
                }
            }

            // Bottom control description and action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = when (breathePhase) {
                        "Başla" -> "Zihnini kozmik akışa hazırlamak için dokun..."
                        "Nefes Al" -> "Yavaşça NEFES AL... Ciğerlerini doldur"
                        "Tut" -> "TUT NEFESİNİ... Kendine odaklan"
                        "Nefes Ver" -> "Sakince NEFES VER... Tüm stresi bırak"
                        else -> ""
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = glowColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (breathePhase == "Başla") {
                    Button(
                        onClick = {
                            breathePhase = "Nefes Al"
                            secondsRemaining = 4
                            scaleValue = 1.0f // Target scale for inhale
                            
                            runningJob = coroutineScope.launch {
                                while (isActive) {
                                    // 1. Inhale loop (4s)
                                    breathePhase = "Nefes Al"
                                    scaleValue = 1.0f
                                    for (i in 4 downTo 1) {
                                        secondsRemaining = i
                                        delay(1000)
                                    }
                                    
                                    // 2. Hold loop (4s)
                                    breathePhase = "Tut"
                                    scaleValue = 1.0f
                                    for (i in 4 downTo 1) {
                                        secondsRemaining = i
                                        delay(1000)
                                    }
                                    
                                    // 3. Exhale loop (4s)
                                    breathePhase = "Nefes Ver"
                                    scaleValue = 0.6f
                                    for (i in 4 downTo 1) {
                                        secondsRemaining = i
                                        delay(1000)
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CelestialAmber, contentColor = SpaceOnyx),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp).fillMaxWidth().testTag("start_breathe_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Egzersizi Başlat", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            runningJob?.cancel()
                            runningJob = null
                            breathePhase = "Başla"
                            scaleValue = 0.6f
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp).fillMaxWidth().testTag("stop_breathe_btn")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Egzersizi Bitir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================
// SECTION 2: INTERACTIVE ORACLE / WISDOM CARDS
// ============================================
@Composable
fun CosmicWisdomCardSection() {
    val whispers = remember {
        listOf(
            CosmicWhisper(
                id = 1,
                message = "Unutma, bugün seni uykusuz bırakan o büyük zorluklar, 5 yıl sonraki benim en gurur duyduğum hikayem oldu. Hepsi geçti.",
                source = "5 Yıl Sonraki Sen",
                icon = "✨",
                cardGradientColors = listOf(Color(0xFF3F2B96), Color(0xFFA8C0FF))
            ),
            CosmicWhisper(
                id = 2,
                message = "Devasa sıçramalar hayal etme. Her gün attığın o sarsak ama inançlı küçük adımlar var ya, işte onlar beni buraya, bu harika zirveye getirdi.",
                source = "5 Yıl Sonraki Sen",
                icon = "🏆",
                cardGradientColors = listOf(Color(0xFF11998e), Color(0xFF38ef7d))
            ),
            CosmicWhisper(
                id = 3,
                message = "Şu an içinde filizlenen o tatlı kaygı ve heyecan var ya... İşte o, gelecekte kollarımızı açarak karşılayacağımız eşsiz bir hayatın ilk kıvılcımıdır.",
                source = "Geleceğin Sesi",
                icon = "🔥",
                cardGradientColors = listOf(Color(0xFF8A2387), Color(0xFFE94057))
            ),
            CosmicWhisper(
                id = 4,
                message = "Bugün kendine biraz daha şefkat göstermeyi hak ediyorsun. İnan bana, şu an elinden gelenin en iyisini yapıyorsun ve ben seninle her an gurur duyuyorum.",
                source = "Bilge Benliğin",
                icon = "💖",
                cardGradientColors = listOf(Color(0xFFff007f), Color(0xFFff85a2))
            ),
            CosmicWhisper(
                id = 5,
                message = "Tökezlediğinde kendine yenildiğini söyleme. Yolculuk esnasında durup bir soluklanmak yarışı kaybettiğin anlamına gelmez. Sadece ciğerlerini doldur.",
                source = "Kozmik Pusula",
                icon = "🛡️",
                cardGradientColors = listOf(Color(0xFF43C6AC), Color(0xFF191654))
            ),
            CosmicWhisper(
                id = 6,
                message = "Sınırlar, yalnızca senin zihninde çizdiğin sınırlardan ibarettir. 5 yıl sonraki hayatımda aşamayacağımı düşündüğüm kaç kapının kendiliğinden açıldığını görsen inanamazdın.",
                source = "Kozmik Benliğin",
                icon = "🚀",
                cardGradientColors = listOf(Color(0xFFED213A), Color(0xFF93291E))
            ),
            CosmicWhisper(
                id = 7,
                message = "Geleceğini şekillendirirken telaşa kapılma. Hayatta her şey mükemmel bir zamanlama ile ilerliyor. Doğru vakitte, tam da hayal ettiğin yerde olacağız.",
                source = "Kozmik Pusula",
                icon = "⏳",
                cardGradientColors = listOf(Color(0xFF00c6ff), Color(0xFF0072ff))
            ),
            CosmicWhisper(
                id = 8,
                message = "Başkalarının ne dediği veya ne düşündüğü zamanla o kadar önemsizleşiyor ki... Sen yalnızca kalbinin sesini dinle ve bana güven.",
                source = "5 Yıl Sonraki Sen",
                icon = "👁️",
                cardGradientColors = listOf(Color(0xFFf12711), Color(0xFFf5af19))
            )
        )
    }

    var currentCardIdx by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(SpaceOnyx)
            .border(BorderStroke(1.dp, NebulaDeep), RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Kozmik Fısıltı Kartı",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Geleceğinden sana özel bir tavsiye kartı çekmek için aşağıdaki gizemli karta dokun.",
                fontSize = 11.sp,
                color = MoonlightGray,
                textAlign = TextAlign.Center
            )
        }

        // Shimmering Oracle Card Graphic
        val activeWhisper = whispers[currentCardIdx]
        
        Card(
            modifier = Modifier
                .width(220.dp)
                .height(300.dp)
                .clickable { isFlipped = !isFlipped }
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .testTag("oracle_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isFlipped) {
                            Brush.verticalGradient(activeWhisper.cardGradientColors)
                        } else {
                            Brush.verticalGradient(listOf(NebulaDeep, SpaceOnyx, MidnightBlack))
                        }
                    )
                    .border(
                        BorderStroke(
                            width = 2.dp,
                            color = if (isFlipped) Color.White.copy(alpha = 0.6f) else CelestialAmber
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isFlipped) {
                    // CARD BACKSIDE - Shimmering Mystical Cover
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🔮",
                            fontSize = 46.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "GELECEKTEN\nBİR KART SEÇ",
                            textAlign = TextAlign.Center,
                            color = CelestialAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Açmak için dokun",
                            color = MoonlightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // CARD FRONTSIDE - Inspirational Wisdom
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeWhisper.source,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = activeWhisper.icon,
                                fontSize = 16.sp
                            )
                        }

                        Text(
                            text = "“${activeWhisper.message}”",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 21.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                        )

                        Text(
                            text = "5 Yıl Sonraki Senin Günlüğü",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.61f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Draw new card buttons
        Button(
            onClick = {
                val nextIdx = (currentCardIdx + 1) % whispers.size
                currentCardIdx = nextIdx
                isFlipped = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = NebulaDeep, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(44.dp)
                .fillMaxWidth()
                .testTag("draw_next_card_btn")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = CelestialAmber)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Başka Bir Fısıltı Al", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// ============================================
// SECTION 3: SOLFEGGIO FREQUENCY GENERATOR SCREEN
// ============================================
@Composable
fun CosmicFrequencySection() {
    val frequencies = remember {
        listOf(
            FrequencyItem(
                freq = 396.0,
                title = "396 Hz - Kurtuluş Frekansı",
                slogan = "Korku ve Suçluluktan Arınma",
                benefit = "İçsel blokajlarını eritip, geleceğe zihninde hiçbir korku duymadan adım atmana yardım eder.",
                emoji = "🛡️",
                color = SoftPink
            ),
            FrequencyItem(
                freq = 432.0,
                title = "432 Hz - Doğa Akordu",
                slogan = "Zihinsel Uyum ve Netlik",
                benefit = "Zihnindeki dalgaları yatıştırır, evrensel frekanslarla seni bağlar ve odaklanmayı kolaylaştırır.",
                emoji = "🌲",
                color = CalmTeal
            ),
            FrequencyItem(
                freq = 528.0,
                title = "528 Hz - Mucize Frekansı",
                slogan = "Dönüşüm ve Hücre Enerjisi",
                benefit = "Kozmik motivasyonun tepe noktasıdır. Hücresel şifa ve enerjiyi tetikler, niyetlerini güçlendirir.",
                emoji = "✨",
                color = GoldenSun
            ),
            FrequencyItem(
                freq = 639.0,
                title = "639 Hz - Kalp Frekansı",
                slogan = "Kozmik Bağlantı & İletişim",
                benefit = "Sevdiklerinle, geleceğinle ve kendinle olan kalbi bağlarını uyumlu hale getirir, iç barış sağlar.",
                emoji = "💖",
                color = Color(0xFFA8C0FF)
            )
        )
    }

    val activeFreq by CosmicFrequencyEngine.activeFrequency

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(SpaceOnyx)
            .border(BorderStroke(1.dp, NebulaDeep), RoundedCornerShape(24.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Kozmik Şifa Frekansları",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Medite olurken arkada çalması için solfej tonlarını dinle. Ses dalgaları zihnini geleceğe açar.",
                fontSize = 11.sp,
                color = MoonlightGray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(frequencies) { item ->
                val isPlayingThis = activeFreq == item.freq
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { CosmicFrequencyEngine.toggleFrequency(item.freq) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPlayingThis) NebulaDeep else SpaceOnyx.copy(alpha = 0.7f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isPlayingThis) item.color else NebulaDeep
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji circle icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(item.color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item.emoji, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Text content section
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = if (isPlayingThis) item.color else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = item.slogan,
                                color = MoonlightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.benefit,
                                color = MoonlightGray.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Audio Playing state icon container
                        IconButton(
                            onClick = { CosmicFrequencyEngine.toggleFrequency(item.freq) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (isPlayingThis) {
                                // Pulsing music icon or active stop button
                                Icon(
                                    imageVector = Icons.Default.PauseCircleFilled,
                                    contentDescription = "Stop",
                                    tint = item.color,
                                    modifier = Modifier.size(28.dp)
                               )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleFilled,
                                    contentDescription = "Play",
                                    tint = MoonlightGray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (activeFreq != null) {
            Button(
                onClick = { CosmicFrequencyEngine.stop() },
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(38.dp)
            ) {
                Icon(Icons.Default.HighlightOff, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tüm Şifa Seslerini Sustur", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
