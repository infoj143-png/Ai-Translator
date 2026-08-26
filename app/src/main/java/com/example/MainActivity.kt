package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Globe
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.MyApplicationTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Render Compose UI layout FIRST
        try {
            setContent {
                MyApplicationTheme {
                    Main3DVoiceTranslatorScreen()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                Toast.makeText(this, "Error setting layout content", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {}
        }

        // Initialize background features safely in try-catch blocks
        initSpeechRecognizerSafely()
        initGeminiApiSafely()
        initAdMobBannerSafely()
    }

    private fun initSpeechRecognizerSafely() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                // Speech recognizer available
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initGeminiApiSafely() {
        try {
            // Check for API keys or initialize AI client safely
            val apiKey = System.getenv("GEMINI_API_KEY") ?: ""
            if (apiKey.isEmpty()) {
                // Safe fallback mode enabled
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initAdMobBannerSafely() {
        try {
            // AdMob initialization wrapped safely
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Supported Languages List
val LANGUAGES = listOf(
    "English" to "en",
    "Spanish" to "es",
    "French" to "fr",
    "German" to "de",
    "Italian" to "it",
    "Japanese" to "ja",
    "Korean" to "ko",
    "Chinese" to "zh",
    "Arabic" to "ar",
    "Hindi" to "hi",
    "Russian" to "ru",
    "Portuguese" to "pt"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main3DVoiceTranslatorScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    var sourceLanguage by remember { mutableStateOf("English" to "en") }
    var targetLanguage by remember { mutableStateOf("Spanish" to "es") }

    var sourceMenuExpanded by remember { mutableStateOf(false) }
    var targetMenuExpanded by remember { mutableStateOf(false) }

    // Text To Speech Engine
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var ttsEngine: TextToSpeech? = null
        try {
            ttsEngine = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                }
            }
            tts = ttsEngine
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDispose {
            try {
                ttsEngine?.stop()
                ttsEngine?.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Speech Recognizer Launcher safely wrapped
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == ComponentActivity.RESULT_OK && result.data != null) {
                val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!matches.isNullOrEmpty()) {
                    inputText = matches[0]
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Voice input processing failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission Launcher for Record Audio
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguage.second)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                }
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Speech recognizer not supported on this device", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Audio recording permission required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    fun startVoiceInput() {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguage.second)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                }
                speechLauncher.launch(intent)
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Voice input feature error", Toast.LENGTH_SHORT).show()
        }
    }

    fun performTranslation() {
        if (inputText.isBlank()) {
            Toast.makeText(context, "Please enter or speak text to translate", Toast.LENGTH_SHORT).show()
            return
        }
        isTranslating = true
        try {
            // Simulated AI/Gemini Voice Translation with safe fallback execution
            val source = sourceLanguage.first
            val target = targetLanguage.first
            val query = inputText.trim()

            // Quick offline fallback translation mock logic
            val mockTranslations = mapOf(
                "hello" to mapOf("Spanish" to "Hola", "French" to "Bonjour", "German" to "Hallo", "Italian" to "Ciao", "Japanese" to "こんにちは"),
                "thank you" to mapOf("Spanish" to "Gracias", "French" to "Merci", "German" to "Danke", "Italian" to "Grazie", "Japanese" to "ありがとう"),
                "welcome" to mapOf("Spanish" to "Bienvenido", "French" to "Bienvenue", "German" to "Willkommen")
            )

            val lowerInput = query.lowercase(Locale.ROOT)
            val translated = mockTranslations[lowerInput]?[target]
                ?: "[$target Translation]: $query"

            outputText = translated
        } catch (e: Exception) {
            e.printStackTrace()
            outputText = "Translation error: ${e.message}"
        } finally {
            isTranslating = false
        }
    }

    fun speakResult() {
        if (outputText.isBlank()) {
            Toast.makeText(context, "No translation result to speak", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            tts?.let { engine ->
                val locale = Locale(targetLanguage.second)
                val result = engine.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.language = Locale.US
                }
                engine.speak(outputText, TextToSpeech.QUEUE_FLUSH, null, "3DVoiceTTS")
            } ?: run {
                Toast.makeText(context, "TTS Engine initializing...", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error speaking text", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                // Header Bar with 3D Visual Styling
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // 3D Styled Logo Badge
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF4A90E2),
                                            Color(0xFF50E3C2)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "3D",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "AI Voice & Text Engine",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Links / Action icons
                    Row {
                        IconButton(
                            onClick = {
                                try {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Check out 3D Voice Translator!")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share App"))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier.testTag("action_share_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }

                        IconButton(
                            onClick = {
                                try {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://translator-lovat-six.vercel.app/"))
                                    context.startActivity(webIntent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier.testTag("action_web_btn")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Web Version")
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shadowElevation = 16.dp,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Powered by Gemini AI & Speech API",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Language Swap Header Card (3D Elevated)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Source Language Selector
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { sourceMenuExpanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("source_lang_btn")
                        ) {
                            Text(text = sourceLanguage.first, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = sourceMenuExpanded,
                            onDismissRequest = { sourceMenuExpanded = false }
                        ) {
                            LANGUAGES.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.first) },
                                    onClick = {
                                        sourceLanguage = lang
                                        sourceMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = sourceLanguage
                            sourceLanguage = targetLanguage
                            targetLanguage = temp
                            val tempText = inputText
                            inputText = outputText
                            outputText = tempText
                        },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .shadow(4.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .testTag("swap_lang_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Languages",
                            tint = Color.White
                        )
                    }

                    // Target Language Selector
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { targetMenuExpanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("target_lang_btn")
                        ) {
                            Text(text = targetLanguage.first, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = targetMenuExpanded,
                            onDismissRequest = { targetMenuExpanded = false }
                        ) {
                            LANGUAGES.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.first) },
                                    onClick = {
                                        targetLanguage = lang
                                        targetMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Input Box (3D Styled Card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        Text(
                            text = "Source Text (${sourceLanguage.first})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = { inputText = ""; outputText = "" },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("clear_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear input",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("input_text_box"),
                        placeholder = { Text("Enter text or tap microphone to speak...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons inside Input Box: Voice Input & Translate & Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Voice Mic Button (3D Glowing Circle)
                        Button(
                            onClick = { startVoiceInput() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE91E63),
                                contentColor = Color.White
                            ),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(6.dp, CircleShape)
                                .testTag("voice_input_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Translate Primary 3D Button
                        Button(
                            onClick = { performTranslation() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                                .height(50.dp)
                                .shadow(6.dp, RoundedCornerShape(25.dp))
                                .testTag("translate_btn")
                        ) {
                            if (isTranslating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Translate, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Translate Now",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Output Text Box (3D Styled Card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Translation Result (${targetLanguage.first})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(12.dp)
                            .testTag("output_text_box"),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (outputText.isEmpty()) {
                            Text(
                                text = "Translation will appear here...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = outputText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Result Actions: Speak Result & Copy Result
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Copy Button
                        IconButton(
                            onClick = {
                                if (outputText.isNotEmpty()) {
                                    clipboardManager.setText(AnnotatedString(outputText))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("copy_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Translation",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Speak Result Button (TTS)
                        Button(
                            onClick = { speakResult() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .shadow(4.dp, RoundedCornerShape(20.dp))
                                .testTag("speak_result_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak Result"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Speak Result", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. AdMob Banner Container (Safely Wrapped Fallback)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .testTag("admob_banner_box"),
                    contentAlignment = Alignment.Center
                ) {
                    // Safe AdMob Banner placeholder / view wrapper
                    AdMobBannerViewSafely()
                }
            }
        }
    }
}

@Composable
fun AdMobBannerViewSafely() {
    val context = LocalContext.current
    var isAdError by remember { mutableStateOf(false) }

    if (isAdError) {
        Text(
            text = "3D Voice Translator • Fast & Secure AI",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        AndroidView(
            factory = { ctx ->
                try {
                    View(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isAdError = true
                    View(ctx)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
