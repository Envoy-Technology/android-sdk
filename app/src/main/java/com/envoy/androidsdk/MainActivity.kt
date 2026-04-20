package com.envoy.androidsdk

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.envoy.androidsdk.screenshot.ScreenshotDetector
import com.envoy.androidsdk.ui.theme.AndroidSdkTheme

data class ButtonState(
    val text: String,
    val onClick: () -> Unit
)

class MainActivity : ComponentActivity() {

    private var screenshotDetector: ScreenshotDetector? = null
    private lateinit var viewModel: MainViewModel

    private val requestMediaPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d(TAG, "Media permission granted: $granted")
            if (granted) {
                screenshotDetector?.start()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel()

        // Initialize screenshot detector
        // Note: Requires READ_EXTERNAL_STORAGE or READ_MEDIA_IMAGES permission
        screenshotDetector = ScreenshotDetector(contentResolver) { bitmap ->
            Log.d(TAG, "Screenshot detected!")
            viewModel.createScreenshotLink(bitmap)
        }

        ensureMediaPermission()

        setContent {
            AndroidSdkTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EnvoyApiProviderImpl.init(
                        apiKey = "lyEEDHBxog54O1VTWmgey770BvTdZqYG5GAibgRk",
                        context = applicationContext
                    )
                    Greeting(data = viewModel.getButtonsState())
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (hasMediaPermission()) {
            screenshotDetector?.start()
        }
    }

    override fun onStop() {
        super.onStop()
        screenshotDetector?.stop()
    }

    private fun mediaPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private fun hasMediaPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, mediaPermission()) ==
            PackageManager.PERMISSION_GRANTED

    private fun ensureMediaPermission() {
        if (!hasMediaPermission()) {
            requestMediaPermission.launch(mediaPermission())
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
