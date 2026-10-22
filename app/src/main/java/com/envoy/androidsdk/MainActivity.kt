package com.envoy.androidsdk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.envoy.androidsdk.sample.BuildConfig
import com.envoy.androidsdk.ui.theme.AndroidSdkTheme

data class ButtonState(
    val text: String,
    val onClick: () -> Unit
)

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        EnvoyApiProviderImpl.init(
            apiKey = BuildConfig.API_KEY,
            context = applicationContext
        )
        EnvoyApiProviderImpl.provide().checkDeferredLink { found ->
            Log.d(TAG, "Deferred link found: $found")
        }

        viewModel = MainViewModel()
        val openArticle = ButtonState(
            text = "Open article (screenshot sharing)",
            onClick = { startActivity(Intent(this, ArticleActivity::class.java)) }
        )

        setContent {
            AndroidSdkTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(data = listOf(openArticle) + viewModel.getButtonsState())
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun Greeting(
    data: List<ButtonState>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        data.forEach {
            Button(onClick = it.onClick) {
                Text(text = it.text)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidSdkTheme {
        Greeting(data = listOf())
    }
}
