package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChatMessage
import com.example.data.Goal
import com.example.data.JournalEntry
import kotlinx.coroutines.launch

// Beautiful Dark Cosmic Color Palette
val MidnightBlack = Color(0xFF0D0E13)
val SpaceOnyx = Color(0xFF141521)
val NebulaDeep = Color(0xFF1E2036)
val CelestialAmber = Color(0xFFFFB347)
val GoldenSun = Color(0xFFFFD700)
val MoonlightGray = Color(0xFFBAC0D6)
val SoftPink = Color(0xFFFF7E9B)
val CalmTeal = Color(0xFF3CD1C4)
val SunsetOrange = Color(0xFFE05252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureSelfApp(
    viewModel: FutureSelfViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf("Sohbet") } // Sohbet, Günlük, CosmicOda, Hedefler, Mektup

    // Background Gradient with subtle animation or stationary cosmic brushes
    val bgGradient = Brush.verticalGradient(
        colors = listOf(MidnightBlack, SpaceOnyx, NebulaDeep)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = SpaceOnyx,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Navigation items with robust target size and test tags
                NavigationBarItem(
                    selected = currentTab == "Sohbet",
                    onClick = { currentTab = "Sohbet" },
                    icon = { Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Sohbet Tab") },
                    label = { Text("Sohbet", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_chat_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CelestialAmber,
                        selectedTextColor = CelestialAmber,
                        indicatorColor = NebulaDeep,
                        unselectedIconColor = MoonlightGray,
                        unselectedTextColor = MoonlightGray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "Günlük",
                    onClick = { currentTab = "Günlük" },
                    icon = { Icon(Icons.Outlined.Book, contentDescription = "Günlük Tab") },
                    label = { Text("Günlük", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_journal_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CelestialAmber,
                        selectedTextColor = CelestialAmber,
                        indicatorColor = NebulaDeep,
                        unselectedIconColor = MoonlightGray,
                        unselectedTextColor = MoonlightGray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "CosmicOda",
                    onClick = { currentTab = "CosmicOda" },
                    icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = "Kozmik Oda Tab") },
                    label = { Text("Kozmik Oda", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_cosmic_oda_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CelestialAmber,
                        selectedTextColor = CelestialAmber,
                        indicatorColor = NebulaDeep,
                        unselectedIconColor = MoonlightGray,
                        unselectedTextColor = MoonlightGray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "Hedefler",
                    onClick = { currentTab = "Hedefler" },
                    icon = { Icon(Icons.Outlined.Timeline, contentDescription = "Hedefler Tab") },
                    label = { Text("Yol Haritası", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_goals_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CelestialAmber,
                        selectedTextColor = CelestialAmber,
                        indicatorColor = NebulaDeep,
                        unselectedIconColor = MoonlightGray,
                        unselectedTextColor = MoonlightGray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "Mektup",
                    onClick = { currentTab = "Mektup" },
                    icon = { Icon(Icons.Outlined.Mail, contentDescription = "Mektup Tab") },
                    label = { Text("Gelecek Mektubu", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_letter_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CelestialAmber,
                        selectedTextColor = CelestialAmber,
                        indicatorColor = NebulaDeep,
                        unselectedIconColor = MoonlightGray,
                        unselectedTextColor = MoonlightGray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "ScreenTransitions"
            ) { tab ->
                when (tab) {
                    "Sohbet" -> ChatScreen(viewModel)
                    "Günlük" -> JournalScreen(viewModel)
                    "CosmicOda" -> CosmicOdaScreen()
                    "Hedefler" -> GoalsScreen(viewModel)
                    "Mektup" -> LetterScreen(viewModel)
                }
            }
        }
    }
}

// ============================================
// SCREEN 1: CHAT SCREEN (Turkish Future Self Chat)
// ============================================
@Composable
fun ChatScreen(viewModel: FutureSelfViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var textInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Scroll to bottom when a new message appears
    LaunchedEffect(messages.size, isChatLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var showClearConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpaceOnyx)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(GoldenSun, CelestialAmber)))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.HourglassEmpty,
                    contentDescription = "Future Self Avatar",
                    tint = SpaceOnyx,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gelecekteki Sen (5 Yıl Sonra)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.Green, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Seninle bağlantıda",
                        color = MoonlightGray,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = { showClearConfirm = true },
                modifier = Modifier.testTag("chat_clear_history_btn")
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Sohbeti Temizle", tint = SunsetOrange)
            }
        }

        // Chat History List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty() && !isChatLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 24.dp)
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Geleceğe Hoş Geldin! 🔮",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "5 yıl sonraki kendinle dertleşmek, sorular sormak ve ondan bilgelik dolu tavsiyeler almak üzeresin.",
                            color = MoonlightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "SOHBET BAŞLATMAK İÇİN SEÇ:",
                            color = CelestialAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val suggestions = listOf(
                            "Bugün doğru yaptığım şeyler neler? Bana 5 yıl sonradan seslen.",
                            "Şu an çok kaygılıyım, bana sakinleştirici bir nasihat verir misin?",
                            "Hedeflerimi nasıl planlamalıyım? Tavsiyelerin nelerdir?",
                            "5 yıl sonra nerede olacağız? Çok merak ediyorum!"
                        )

                        suggestions.forEach { suggestion ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable {
                                        viewModel.sendChatMessage(suggestion)
                                    },
                                colors = CardDefaults.cardColors(containerColor = SpaceOnyx),
                                border = BorderStroke(1.dp, NebulaDeep),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CelestialAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = suggestion,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = MoonlightGray, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(GoldenSun, CelestialAmber))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🌟", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(
                        modifier = Modifier.widthIn(max = 280.dp),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = if (isUser) CelestialAmber else NebulaDeep,
                            contentColor = if (isUser) SpaceOnyx else Color.White,
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (isUser) Color.Transparent else SpaceOnyx,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                MessageContent(messageText = msg.message)
                            }
                        }
                    }

                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(GoldenSun, CelestialAmber))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✍️", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NebulaDeep),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = CelestialAmber
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Zaman çizgisinden yanıt geliyor...",
                                    fontSize = 12.sp,
                                    color = MoonlightGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Chat Input Area
        Surface(
            color = SpaceOnyx,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Geleceğine bir şey sor veya içini dök...", color = MoonlightGray) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MidnightBlack,
                        unfocusedContainerColor = MidnightBlack,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (textInput.trim().isNotEmpty()) {
                            viewModel.sendChatMessage(textInput)
                            textInput = ""
                        }
                    },
                    containerColor = CelestialAmber,
                    contentColor = SpaceOnyx,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send Message",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = SpaceOnyx,
            title = { Text("Zaman Hafızasını Sıfırla?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Gelecekteki kendinle yaptığın tüm sohbetleri silmek istediğine emin misin? Zaman akışımız tamamen temizlenecek.", color = MoonlightGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChatHistory()
                        showClearConfirm = false
                    },
                    modifier = Modifier.testTag("confirm_clear_btn")
                ) {
                    Text("Evet, Temizle", color = SunsetOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Vazgeç", color = Color.White)
                }
            }
        )
    }
}

// ============================================
// SCREEN 2: JOURNAL & DAY LOGGER SCREEN
// ============================================
@Composable
fun JournalScreen(viewModel: FutureSelfViewModel) {
    val entries by viewModel.journalEntries.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Info & Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Zaman Günlüğü",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Bugünkü duygularını, zorluklarını ve başarılarını günlüğe yaz. Gelecekteki sen bu izleri takip edecek.",
                    fontSize = 13.sp,
                    color = MoonlightGray,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (entries.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Icon(
                            Icons.Default.BorderColor,
                            contentDescription = "No Entries",
                            tint = MoonlightGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Henüz zaman günlüğün boş.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bugünü geleceğine fısıldamak için aşağıdaki kalem düğmesinden ilk kaydını oluştur.",
                            color = MoonlightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        JournalItemCard(entry = entry, onDelete = {
                            viewModel.deleteJournalEntry(entry.id)
                        })
                    }
                }
            }
        }

        // Add Journal floating action button (48dp+ target)
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_journal_fab"),
            containerColor = CelestialAmber,
            contentColor = SpaceOnyx,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Yeni Günlük Yaz")
        }
    }

    if (showAddDialog) {
        AddJournalDialog(
            onDismiss = { showAddDialog = false },
            onSave = { content, mood, wins, struggles ->
                viewModel.addJournalEntry(content, mood, wins, struggles)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun JournalItemCard(entry: JournalEntry, onDelete: () -> Unit) {
    val moodEmoji = when(entry.mood) {
        "Mutlu" -> "🌟"
        "Normal" -> "☕"
        "Huzurlu" -> "💭"
        "Endişeli" -> "🌧️"
        "Stresli" -> "⚡"
        "Heyecanlı" -> "🔥"
        else -> "📖"
    }

    val moodColor = when(entry.mood) {
        "Mutlu" -> CelestialAmber
        "Normal" -> MoonlightGray
        "Huzurlu" -> CalmTeal
        "Endişeli" -> Color(0xFF5390F5)
        "Stresli" -> SunsetOrange
        "Heyecanlı" -> Color(0xFFF39C12)
        else -> CelestialAmber
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpaceOnyx),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, NebulaDeep)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mood Badge
                Row(
                    modifier = Modifier
                        .background(moodColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .border(1.dp, moodColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "$moodEmoji ", fontSize = 12.sp)
                    Text(text = entry.mood, fontSize = 11.sp, color = moodColor, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.date,
                    color = MoonlightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Günlüğü Sil",
                        tint = SunsetOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = entry.content,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal
            )

            if (entry.wins.isNotEmpty() || entry.struggles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = NebulaDeep, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                if (entry.wins.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "💚 ", fontSize = 13.sp)
                        Column {
                            Text(text = "Bugünün Güzelliği / Başarısı:", fontSize = 11.sp, color = CalmTeal, fontWeight = FontWeight.Bold)
                            Text(text = entry.wins, color = MoonlightGray, fontSize = 12.sp, modifier = Modifier.padding(top = 1.dp))
                        }
                    }
                }

                if (entry.struggles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "🧡 ", fontSize = 13.sp)
                        Column {
                            Text(text = "Zorlayıcı Kısım / Kaygı:", fontSize = 11.sp, color = SunsetOrange, fontWeight = FontWeight.Bold)
                            Text(text = entry.struggles, color = MoonlightGray, fontSize = 12.sp, modifier = Modifier.padding(top = 1.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddJournalDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("Huzurlu") }
    var wins by remember { mutableStateOf("") }
    var struggles by remember { mutableStateOf("") }

    val moodList = listOf(
        "Mutlu" to "🌟",
        "Huzurlu" to "💭",
        "Normal" to "☕",
        "Endişeli" to "🌧️",
        "Stresli" to "⚡",
        "Heyecanlı" to "🔥"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = SpaceOnyx,
            border = BorderStroke(1.dp, NebulaDeep)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .wrapContentHeight()
            ) {
                Text(
                    text = "Bugünü Geleceğine Aktar",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mood Selection Row (wrapped flow for reliability)
                Text(
                    text = "Bugün nasıl hissediyorsun?",
                    fontSize = 12.sp,
                    color = MoonlightGray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moodList.forEach { (moodName, emoji) ->
                        val isSelected = selectedMood == moodName
                        Row(
                            modifier = Modifier
                                .background(
                                    if (isSelected) CelestialAmber.copy(alpha = 0.2f) else NebulaDeep,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CelestialAmber else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMood = moodName }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = moodName,
                                fontSize = 12.sp,
                                color = if (isSelected) CelestialAmber else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Journal note Block
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Bugün neler yaşadın? (Günlük)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("journal_input_content"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelestialAmber,
                        unfocusedBorderColor = NebulaDeep,
                        focusedLabelColor = CelestialAmber,
                        unfocusedLabelColor = MoonlightGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Custom extra components: Wins & Challenges
                OutlinedTextField(
                    value = wins,
                    onValueChange = { wins = it },
                    placeholder = { Text("Zorluklara rağmen başardığın ne var?", color = MoonlightGray.copy(alpha = 0.5f)) },
                    label = { Text("Bugünün Başarısı/Güzelliği", color = CalmTeal) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_input_wins"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CalmTeal,
                        unfocusedBorderColor = NebulaDeep,
                        focusedLabelColor = CalmTeal,
                        unfocusedLabelColor = MoonlightGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = struggles,
                    onValueChange = { struggles = it },
                    placeholder = { Text("Kafanı karıştıran veya üzen nedir?", color = MoonlightGray.copy(alpha = 0.5f)) },
                    label = { Text("Bugünün Kaygısı/Mücadelesi", color = SunsetOrange) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_input_struggles"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SunsetOrange,
                        unfocusedBorderColor = NebulaDeep,
                        focusedLabelColor = SunsetOrange,
                        unfocusedLabelColor = MoonlightGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Vazgeç", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (content.trim().isNotEmpty()) {
                                onSave(content, selectedMood, wins, struggles)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CelestialAmber, contentColor = SpaceOnyx),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_journal_btn")
                    ) {
                        Text("Geleceğe Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================
// SCREEN 3: MILESTONES & ROADMAP (Hedef Yol Haritası)
// ============================================
@Composable
fun GoalsScreen(viewModel: FutureSelfViewModel) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    var showAddGoalDialog by remember { mutableStateOf(false) }

    // Statistics logic
    val completedCount = goals.count { it.isAchieved }
    val totalCount = goals.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Gelecek Haritası",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "5 yıllık gelişim adımlarını planla. Tamamladığın hedefleri işaretledikçe, 5 yıl sonraki versiyonunun hayata geldiğini hisset.",
                fontSize = 13.sp,
                color = MoonlightGray,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpaceOnyx),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, NebulaDeep)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zaman Çizelgesi Gelişimi",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "% ${(progress * 100).toInt()}",
                            color = CelestialAmber,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = CelestialAmber,
                        trackColor = NebulaDeep
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (totalCount == 0) "Henüz bir hedef girmedin." else "$totalCount hedeften $completedCount tanesi başarıldı!",
                        color = MoonlightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gelecek Miltaşları",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // Button to Add Goal with 48dp target
            IconButton(
                onClick = { showAddGoalDialog = true },
                modifier = Modifier
                    .background(CelestialAmber, RoundedCornerShape(8.dp))
                    .size(36.dp)
                    .testTag("add_goal_icon_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal Icon", tint = SpaceOnyx)
            }
        }

        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = "No Goals",
                        tint = MoonlightGray.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Henüz yol haritası çizmedin.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kariyer, sağlık ya da zihinsel gelişim... 5 yılda varmak istediğin durakları yukarıdaki '+' tuşu ile ekle.",
                        color = MoonlightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(goals) { goal ->
                    GoalItemRow(
                        goal = goal,
                        onCheckedChange = { viewModel.toggleGoalAchievement(goal) },
                        onDelete = { viewModel.deleteGoal(goal.id) }
                    )
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, targetYear, category ->
                viewModel.addGoal(title, targetYear, category)
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
fun GoalItemRow(
    goal: Goal,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when(goal.category) {
        "Kariyer" -> CelestialAmber
        "Sağlık" -> CalorGreen
        "İlişkiler" -> SoftPink
        "Zihinsel" -> CalmTeal
        else -> MoonlightGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpaceOnyx),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (goal.isAchieved) NebulaDeep else categoryColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox/Radio selector
            Checkbox(
                checked = goal.isAchieved,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = CelestialAmber,
                    uncheckedColor = MoonlightGray
                ),
                modifier = Modifier.testTag("goal_checkbox_${goal.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    color = if (goal.isAchieved) MoonlightGray else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .background(categoryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = goal.category, color = categoryColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Target Year Time Label
                    Text(
                        text = "${goal.targetYear}. Yıl Hedefi",
                        color = MoonlightGray,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete Goal",
                    tint = SunsetOrange.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Extra Custom Color for Goals category since we want colors to be rich
val CalorGreen = Color(0xFF4EE286)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onSave: (String, Int, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var targetYear by remember { mutableIntStateOf(1) } // 1, 3, 5 target years
    var selectedCategory by remember { mutableStateOf("Kariyer") }

    val categories = listOf("Kariyer", "Sağlık", "İlişkiler", "Zihinsel")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = SpaceOnyx,
            border = BorderStroke(1.dp, NebulaDeep)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Geleceğe Hedef Ekle",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Hedef Başlığı (Örn: Dil öğren, Şirket kur)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelestialAmber,
                        unfocusedBorderColor = NebulaDeep,
                        focusedLabelColor = CelestialAmber,
                        unfocusedLabelColor = MoonlightGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Zamanlama (Hedef Yılı):",
                    fontSize = 12.sp,
                    color = MoonlightGray,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(1, 3, 5).forEach { year ->
                        val isSelected = targetYear == year
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) CelestialAmber.copy(alpha = 0.2f) else NebulaDeep,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CelestialAmber else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { targetYear = year }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$year. Yıl",
                                color = if (isSelected) CelestialAmber else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kategori:",
                    fontSize = 12.sp,
                    color = MoonlightGray,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) CelestialAmber.copy(alpha = 0.2f) else NebulaDeep,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CelestialAmber else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) CelestialAmber else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Vazgeç", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.trim().isNotEmpty()) {
                                onSave(title, targetYear, selectedCategory)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CelestialAmber, contentColor = SpaceOnyx),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_goal_btn")
                    ) {
                        Text("Ekle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================
// SCREEN 4: LETTERS FROM FUTURE (Gelecek Mektupları) & ANALYTICS
// ============================================
@Composable
fun LetterScreen(viewModel: FutureSelfViewModel) {
    val letter by viewModel.letterFromFuture.collectAsStateWithLifecycle()
    val isLetterLoading by viewModel.isLetterLoading.collectAsStateWithLifecycle()
    var subTab by remember { mutableStateOf("Mektup") } // Mektup, Karne

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zaman Portalı",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            
            // Sub tab Selector Row
            Row(
                modifier = Modifier
                    .background(SpaceOnyx, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (subTab == "Mektup") NebulaDeep else Color.Transparent)
                        .clickable { subTab = "Mektup" }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mektup",
                        color = if (subTab == "Mektup") CelestialAmber else MoonlightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (subTab == "Karne") NebulaDeep else Color.Transparent)
                        .clickable { subTab = "Karne" }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Karne 📊",
                        color = if (subTab == "Karne") CelestialAmber else MoonlightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        Text(
            text = if (subTab == "Mektup") 
                "Yazdığın tüm günlüklerden ve duygusal akışlardan süzülen 5 yıllık gelecek bilgelik mektubu."
            else 
                "Gelecekteki senin mesajlarından modellediği haftalık duygu grafiğin ve tetiklenen alışkanlıkların.",
            fontSize = 12.sp,
            color = MoonlightGray,
            lineHeight = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        AnimatedContent(
            targetState = subTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
            },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            label = "SubTabTransitions"
        ) { tabState ->
            if (tabState == "Mektup") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SpaceOnyx, RoundedCornerShape(24.dp))
                        .border(BorderStroke(1.dp, NebulaDeep), RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLetterLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CelestialAmber)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "5 yıl sonraki halin tüm günlüklerini inceliyor ve derin hisleriyle sana yazıyor...",
                                color = Color.White,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else if (letter != null) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "5 Yıl Sonraki Senden...",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = CelestialAmber,
                                    fontSize = 17.sp
                                )
                                Icon(
                                    Icons.Default.WorkspacePremium,
                                    contentDescription = "Mühür",
                                    tint = CelestialAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = NebulaDeep, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Top
                            ) {
                                item {
                                    Text(
                                        text = letter!!,
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Serif,
                                        lineHeight = 22.sp,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.MailOutline,
                                contentDescription = "No Letter Yet",
                                tint = MoonlightGray.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Henüz Özel Mektup Oluşturulmadı",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Günlüklerinden bilgi alarak sadece senin için derinlemesine bir mektup hazırlamamı ister misin? Aşağıdaki tuşa basarak kozmik enerjiyi başlatabilirsin.",
                                color = MoonlightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            } else {
                ZamanKarneView(viewModel)
            }
        }

        if (subTab == "Mektup") {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.generateLetterFromFuture() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CelestialAmber,
                    contentColor = SpaceOnyx
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("request_letter_btn")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (letter == null) "Mektup Kaleme Almasını İste" else "Mektubu Güncelle/Yenile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ============================================
// CHAT PARSING & VALUE STYLING COMPOSABLES
// ============================================

@Composable
fun MessageContent(messageText: String) {
    val text = messageText.trim()
    val isLetter = text.contains("[MEKTUP]")
    val cleanText = text.replace("[MEKTUP]", "").trim()

    val taskMarker = "Gelecekteki Senin Görevi:"
    val hasTask = cleanText.contains(taskMarker)

    if (isLetter) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF6EE)),
            border = BorderStroke(1.dp, Color(0xFFE2CBB7)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✉️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GELECEKTEN MEKTUP",
                            color = Color(0xFF8D6E63),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE0C3FC), RoundedCornerShape(2.dp))
                            .border(1.dp, Color(0xFFB5179E), RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("5", color = Color(0xFFB5179E), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                
                Divider(color = Color(0xFFECE4D9), modifier = Modifier.padding(vertical = 12.dp))

                if (hasTask) {
                    val parts = cleanText.split(taskMarker, limit = 2)
                    val letterBody = parts[0].trim()
                    val taskBody = parts[1].trim()

                    Text(
                        text = letterBody,
                        color = Color(0xFF3E2723),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ChallengeBox(taskBody)
                } else {
                    Text(
                        text = cleanText,
                        color = Color(0xFF3E2723),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    } else {
        Column {
            if (hasTask) {
                val parts = cleanText.split(taskMarker, limit = 2)
                val mainBody = parts[0].trim()
                val taskBody = parts[1].trim()

                if (mainBody.isNotEmpty()) {
                    Text(
                        text = mainBody,
                        fontSize = 14.sp,
                        letterSpacing = 0.2.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                ChallengeBox(taskBody)
            } else {
                Text(
                    text = cleanText,
                    fontSize = 14.sp,
                    letterSpacing = 0.2.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ChallengeBox(taskText: String) {
    var isDone by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) Color(0xFF1B3D30) else Color(0xFF28211E)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDone) Color(0xFF3CDC92) else Color(0xFFFFB347)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable { isDone = !isDone },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isDone) Color(0xFF3CDC92) else Color.Transparent)
                    .border(1.5.dp, if (isDone) Color.Transparent else Color(0xFFFFB347), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Görevi Yapıldı",
                        tint = SpaceOnyx,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GÖREVİNİZ (Gelecekteki Senin Görevi):",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) Color(0xFF89F8C7) else Color(0xFFFFB347),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = taskText,
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Zaman Karnesi Statistics Render Layouts
@Composable
fun ZamanKarneView(viewModel: FutureSelfViewModel) {
    val metrics by viewModel.moodMetrics.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val db = remember { com.example.data.AppDatabase.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()

    if (metrics.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = "No metrics yet",
                tint = MoonlightGray.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Kozmik Karne Boş",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Gelecekteki kendinle dertleştikçe ve ona durumundan bahsettikçe, burada ruh halinin gelişimini gösteren grafikler ve sana özel görevler listelenecek.",
                color = MoonlightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Mood Score Trend Chart Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceOnyx),
                    border = BorderStroke(1.dp, NebulaDeep),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ruh Hali Gelişimin (1-10)",
                            color = CelestialAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gelecekteki senin analiz ettiği gidişatın",
                            color = MoonlightGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        MoodLineChart(metrics = metrics)
                    }
                }
            }

            // 2. Emotion and Habit Breakdown Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SpaceOnyx),
                        border = BorderStroke(1.dp, NebulaDeep),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Tetiklenen Duygular",
                                color = CelestialAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val emotionCounts = metrics.groupBy { it.emotion }
                                .mapValues { it.value.size }
                                .toList()
                                .sortedByDescending { it.second }
                                .take(4)

                            if (emotionCounts.isEmpty()) {
                                Text("Veri yok", color = MoonlightGray, fontSize = 11.sp)
                            } else {
                                emotionCounts.forEach { (emotion, count) ->
                                    val (label, emoji) = getEmotionDetails(emotion)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("$emoji $label", color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("$count kez", color = MoonlightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1.2f),
                        colors = CardDefaults.cardColors(containerColor = SpaceOnyx),
                        border = BorderStroke(1.dp, NebulaDeep),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Önemli Alışkanlıklar",
                                color = CelestialAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val uniqueHabits = metrics.map { it.habit }.distinct().take(4)
                            if (uniqueHabits.isEmpty()) {
                                Text("Veri yok", color = MoonlightGray, fontSize = 11.sp)
                            } else {
                                uniqueHabits.forEach { habit ->
                                    val habitLabel = translateHabitToTurkish(habit)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🧬", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = habitLabel,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Clear stats options
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            db.moodMetricDao().clearMetrics()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = SunsetOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Karne Geçmişini Sıfırla", fontSize = 12.sp)
                }
            }
        }
    }
}

fun translateHabitToTurkish(habit: String): String {
    return when (habit.lowercase()) {
        "self_confidence" -> "Özgüven İnşası"
        "breath_exercise" -> "Nefes Farkındalığı"
        "write_note" -> "Kendine Not"
        "drink_water" -> "Hidrasyon Bilinci"
        "grounding" -> "Zihinsel Topraklanmak"
        "breathing" -> "Sakin Diyafram"
        "celebration" -> "Zafer Kutlaması"
        "self_dialogue" -> "Şefkatli İç Ses"
        else -> habit.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}

fun getEmotionDetails(emotion: String): Pair<String, String> {
    return when (emotion.lowercase()) {
        "anxiety" -> Pair("Endişe", "🥺")
        "joy" -> Pair("Neşe", "😊")
        "sadness" -> Pair("Hüzün", "😢")
        "stress" -> Pair("Stres", "⚡")
        "pride" -> Pair("Gurur", "💪")
        "fear" -> Pair("Korku", "😰")
        "hope" -> Pair("Umut", "✨")
        "loneliness" -> Pair("Yalnızlık", "🌾")
        else -> Pair("Belirsiz", "🔮")
    }
}

@Composable
fun MoodLineChart(metrics: List<com.example.data.MoodMetric>) {
    val data = metrics.takeLast(8)
    
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 8.dp)
    ) {
        val width = size.width
        val height = size.height

        if (data.size < 2) {
            drawLine(
                color = MoonlightGray.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(0f, height / 2f),
                end = androidx.compose.ui.geometry.Offset(width, height / 2f),
                strokeWidth = 2f
            )
            return@Canvas
        }

        val maxVal = 10f
        val stepX = width / (data.size - 1)
        val points = data.mapIndexed { idx, item ->
            val x = idx * stepX
            val y = height - ((item.score.toFloat() / maxVal) * height)
            androidx.compose.ui.geometry.Offset(x, y)
        }

        val guidelines = listOf(0.2f, 0.5f, 0.8f)
        guidelines.forEach { bias ->
            val y = height * bias
            drawLine(
                color = MoonlightGray.copy(alpha = 0.15f),
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(width, y),
                strokeWidth = 1.5f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        val fillPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, height)
            lineTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
            lineTo(points.last().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(CelestialAmber.copy(alpha = 0.35f), Color.Transparent)
            )
        )

        val strokePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = strokePath,
            color = CelestialAmber,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )

        points.forEach { point ->
            drawCircle(
                color = CelestialAmber.copy(alpha = 0.3f),
                radius = 6.dp.toPx(),
                center = point
            )
            drawCircle(
                color = CelestialAmber,
                radius = 3.dp.toPx(),
                center = point
            )
        }
    }
}
