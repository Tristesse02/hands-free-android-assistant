package com.minh.handfreeassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.minh.handfreeassistant.ui.theme.HandFreeAssistantTheme
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf

import android.net.Uri
import android.content.ActivityNotFoundException
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {

    private lateinit var tts: TextToSpeech


    private val recognizedText = mutableStateOf("Nothing heard yet")


    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val matches = result.data?.getStringArrayListExtra(
            RecognizerIntent.EXTRA_RESULTS
        )

        // Update the recognized text first!
        recognizedText.value = matches?.firstOrNull()
            ?: "No speech recognized"

        // Then speak the updated text
        val command = recognizedText.value.lowercase(Locale.US)

        if ("open youtube" in command) {
            openYouTube()
        } else {
            if (::tts.isInitialized) {
                tts.speak(
                    "Sorry, I don't recognize that command yet.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "response"
                )
            }
        }
    }


    private fun openYouTube() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://www.youtube.com".toUri()
        ).apply {
            setPackage("com.google.android.youtube")
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fallback: open YouTube in a browser
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://www.youtube.com".toUri()
                )
            )
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)

                if (result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.speak(
                        "Eyooo Minh, what's up?",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "greeting"
                    )
                }
            }
        }

        setContent {
            HandFreeAssistantTheme {
                Column {
                    Text("Your personal assistant")

                    Button(onClick = {
                        val intent = Intent(
                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                        ).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(
                                RecognizerIntent.EXTRA_PROMPT,
                                "What can I help you with?"
                            )
                        }

                        speechLauncher.launch(intent)
                    }) {
                        Text("Start listening")
                    }

                    Text("You said: ${recognizedText.value}")
                }
            }
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HandFreeAssistantTheme {
        Greeting("Android")
    }
}