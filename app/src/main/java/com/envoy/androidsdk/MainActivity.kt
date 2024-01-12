package com.envoy.androidsdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.envoy.androidsdk.ui.theme.AndroidSdkTheme

data class ButtonState(
    val text: String,
    val onClick: () -> Unit
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = MainViewModel()
            AndroidSdkTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EnvoyApiProviderImpl.init(
                        apiKey = "api_key",
                        context = applicationContext
                    )
                    Greeting(data = viewModel.getButtonsState())
                }
            }
        }
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
