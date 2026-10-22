package com.envoy.androidsdk

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.envoy.androidsdk.domain.model.ContentType
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.screenshot.ScreenshotShare
import com.envoy.androidsdk.ui.theme.AndroidSdkTheme

/** How the gift landing shows a screenshot-shared article: SCREENSHOT shows a screenshot of the page
 * rendered by Envoy, HTML_SOURCE the page itself. */
private val SCREENSHOT_CONTENT_TYPE = ContentType.SCREENSHOT
private const val USER_ID = "123456"

private data class Article(
    val id: String,
    val url: String,
    val title: String,
    val lead: String,
    val imageUrl: String
)

private val article = Article(
    id = "GH5FKRS4XZC5RCYGXK7Q52S7IY",
    url = "https://www.lexpress.fr/idees-et-debats/marie-favereau-poutine-se-reclame-divan-le-terrible-pour-mieux-justifier-son-invasion-GH5FKRS4XZC5RCYGXK7Q52S7IY/",
    title = "Marie Favereau : « Poutine se réclame d'Ivan le Terrible pour mieux justifier son invasion »",
    lead = "Take a screenshot of this article: the share sheet opens with its gift link.",
    imageUrl = "https://www.lexpress.fr/resizer/v2/DGJSJGFOLFANJB6URIILRZBXBM.jpg?auth=fe9f2f627a9e43a9488d79608e29d9c275b888930ade76663a818ad829b9fe60&height=630&quality=85&smart=true&width=1200"
)

/** An article screen: the only screen where a screenshot shares a link. */
class ArticleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // On while this activity is started, off once it's destroyed.
        EnvoyApiProviderImpl.provide().startScreenshotSharing(
            activity = this,
            onEvent = { Log.d(TAG, "Screenshot sharing: $it") }
        ) {
            ScreenshotShare(
                body = CreateLinkBody.forArticle(
                    url = article.url,
                    id = article.id,
                    title = article.title,
                    description = article.lead,
                    sharerId = USER_ID,
                    contentType = SCREENSHOT_CONTENT_TYPE,
                    imageUrl = article.imageUrl,
                    isSandbox = true
                ),
                message = "Your friend wants to gift you this premium article for free."
            )
        }

        setContent {
            AndroidSdkTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(text = article.title, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = article.lead,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "ArticleActivity"
    }
}
