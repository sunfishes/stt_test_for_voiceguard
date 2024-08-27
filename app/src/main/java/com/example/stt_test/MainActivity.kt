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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stt_test.ui.theme.Stt_testTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Stt_testTheme {
                SpeechToTextScreen(this)
            }
        }
    }
}

@Composable
fun SpeechToTextScreen(context: Context) {
    var speechResult by remember { mutableStateOf("음성인식 결과가 여기에 표시됩니다.") }

    // ActivityResultLauncher to handle the STT intent result
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            speechResult = spokenText ?: "음성 인식 실패"
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
    }
}

@Preview(showBackground = true)
@Composable
fun SpeechToTextScreenPreview() {
    Stt_testTheme {
        SpeechToTextScreen(context = LocalContext.current)
    }
}
