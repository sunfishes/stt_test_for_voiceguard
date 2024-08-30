package com.example.stt_test

import android.os.Bundle
import android.speech.RecognizerIntent
import android.content.Intent
import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stt_test.ui.theme.Stt_testTheme

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.compose.material3.Text as Text


class MainActivity : ComponentActivity() {
    private val apiKey = "AIzaSyDsplNEyCGYg7z_1EP564BLthDccRrzY8U"
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.9f
            topK = 1
            topP = 1f
            maxOutputTokens = 2048
        }
    )

    private suspend fun analyzeText(text: String): String {
        val prompt = """
        다음 통화 스크립트를 분석하여 보이스피싱 여부를 판단해주세요:
        [$text]
        """
        return withContext(Dispatchers.IO) {
            try {
                val response = model.generateContent(prompt)
                response.text ?: "분석 결과를 얻지 못했습니다."
            } catch (e: Exception) {
                "오류가 발생했습니다: ${e.message}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Stt_testTheme {
                SpeechToTextScreen(this) { text ->
                    analyzeText(text)
                }
            }
        }
    }
}

@Composable
fun SpeechToTextScreen(context: Context, analyzeText: suspend (String) -> String) {
    var speechResult by remember { mutableStateOf("음성인식 결과가 여기에 표시됩니다.") }
    var analysisResult by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // ActivityResultLauncher to handle the STT intent result
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            speechResult = spokenText ?: "음성 인식 실패"

            scope.launch {
                analysisResult = analyzeText(speechResult)
                showNotification(context, analysisResult)
            }
        } else {
            speechResult = "음성 인식 실패"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = speechResult,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "음성을 입력하세요")
                }
                try {
                    speechRecognizerLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "음성 인식을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(text = "음성인식 시작")
        }

        Text(
            text = analysisResult,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SpeechToTextScreenPreview() {
    Stt_testTheme {
        SpeechToTextScreen(
            context = LocalContext.current,
            analyzeText = { "분석 결과 미리보기" } // 더미 analyzeText 함수 전달
        )
    }
}


fun showNotification(context: Context, message: String) {
    val channelId = "VoicePhishingChannel"
    val notificationId = 1

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Voice Phishing Alerts", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("보이스피싱 분석 결과")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    notificationManager.notify(notificationId, builder.build())
}