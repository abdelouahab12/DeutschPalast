package com.deutschpalast.app

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.deutschpalast.app.data.AppDatabase
import com.deutschpalast.app.data.Word
import java.util.Locale

// Custom speaker vector icon to avoid requiring external libraries
val VolumeUpIcon: ImageVector
    get() = ImageVector.Builder(
        name = "VolumeUp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(3f, 9f)
        lineTo(3f, 15f)
        lineTo(7f, 15f)
        lineTo(12f, 20f)
        lineTo(12f, 4f)
        lineTo(7f, 9f)
        close()
        // Sound waves arcs
        moveTo(16.5f, 12f)
        curveTo(16.5f, 10.23f, 15.48f, 8.71f, 14f, 7.97f)
        lineTo(14f, 16.02f)
        curveTo(15.48f, 15.29f, 16.5f, 13.77f, 16.5f, 12f)
        close()
        moveTo(14f, 3.23f)
        lineTo(14f, 5.29f)
        curveTo(16.89f, 6.15f, 19f, 8.83f, 19f, 12f)
        curveTo(19f, 15.17f, 16.89f, 17.85f, 14f, 18.71f)
        lineTo(14f, 20.77f)
        curveTo(18.01f, 19.86f, 21f, 16.28f, 21f, 12f)
        curveTo(21f, 7.72f, 18.01f, 4.14f, 14f, 3.23f)
        close()
    }.build()

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "deutsch_palast_db"
        ).build()
    }

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory(db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Text-To-Speech engine
        tts = TextToSpeech(this, this)

        setContent {
            MaterialTheme {
                MainScreen(viewModel, ::speakGerman)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.GERMAN)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    private fun speakGerman(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

class ViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(db.wordDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel, speak: (String) -> Unit) {
    val wordsList by viewModel.wordsList.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0C20), // Deep space violet
                        Color(0xFF1B1429)  // Dark plum
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // App Title and Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "قصر الألمانية 🇩🇪",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (!isLoading && wordsList.isNotEmpty() && currentIndex < wordsList.size) {
                    Text(
                        text = "الكلمة ${currentIndex + 1} من ${wordsList.size}",
                        fontSize = 16.sp,
                        color = Color(0xFF8B5CF6), // Glowing purple
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Central Card / Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(color = Color(0xFF8B5CF6))
                    }
                    wordsList.isEmpty() || currentIndex >= wordsList.size -> {
                        // Success Screen
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎉",
                                fontSize = 72.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "أحسنت! لقد انتهيت",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لقد راجعت جميع الكلمات بنجاح لهذا اليوم.",
                                fontSize = 16.sp,
                                color = Color(0xFF9CA3AF),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = { viewModel.resetAllWords() },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8B5CF6)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "إعادة المراجعة من جديد",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    else -> {
                        val currentWord = wordsList[currentIndex]
                        var showMeaning by remember(currentWord.id) { mutableStateOf(false) }

                        // Automatically pronounce German word when it is loaded
                        LaunchedEffect(currentWord.id) {
                            speak(currentWord.german)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .height(300.dp)
                                    .clickable { 
                                        showMeaning = !showMeaning
                                        // Also speak German when user flips card to reveal meaning
                                        speak(currentWord.german)
                                    }
                                    .shadow(24.dp, RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0x15FFFFFF) // Glassmorphism effect
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0x33FFFFFF), Color(0x05FFFFFF))
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // German Word & Pronounce Button Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = currentWord.german,
                                            fontSize = 44.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        IconButton(
                                            onClick = { speak(currentWord.german) },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(Color(0x1EFFFFFF), RoundedCornerShape(12.dp))
                                        ) {
                                            Icon(
                                                imageVector = VolumeUpIcon,
                                                contentDescription = "Pronounce German",
                                                tint = Color(0xFF8B5CF6) // Neon purple
                                            )
                                        }
                                    }

                                    // Display Hint or Meaning
                                    if (!showMeaning) {
                                        Text(
                                            text = "انقر لرؤية المعنى العربي 💡",
                                            fontSize = 14.sp,
                                            color = Color(0x88E2E8F0),
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = currentWord.arabic,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF10B981), // Emerald green
                                                textAlign = TextAlign.Center
                                            )
                                            if (currentWord.example.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    text = currentWord.example,
                                                    fontSize = 15.sp,
                                                    fontStyle = FontStyle.Italic,
                                                    color = Color(0xFF9CA3AF),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }

                            // Decision Buttons
                            AnimatedVisibility(
                                visible = showMeaning,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(0.95f)
                                        .padding(top = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Review Later Button
                                    Button(
                                        onClick = { viewModel.reviewLater(currentWord) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF374151) // Charcoal/slate
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = "أريد مراجعتها لاحقاً",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Mastered Button
                                    Button(
                                        onClick = { viewModel.markAsMastered(currentWord) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF10B981) // Emerald Green
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = "حفظت الكلمة ✓",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Footer branding
            Text(
                text = "Deutsch Palast v1.0",
                fontSize = 12.sp,
                color = Color(0x44FFFFFF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
