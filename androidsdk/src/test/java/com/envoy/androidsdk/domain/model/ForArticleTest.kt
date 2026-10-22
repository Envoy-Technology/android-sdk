package com.envoy.androidsdk.domain.model

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ForArticleTest {

    @Test
    fun sendsTheArticleAsSourceAndItsIdInExtra() {
        val body = CreateLinkBody.forArticle(
            url = "https://news.example/1",
            id = "1",
            title = "Title",
            description = "Description",
            sharerId = "sharer-1",
            contentType = ContentType.HTML_SOURCE,
            imageUrl = "https://news.example/1.jpg",
            extra = mapOf("section" to "world", "articleId" to "old")
        )

        val json = Gson().toJsonTree(body).asJsonObject
        val contentSetting = json.getAsJsonObject("content_setting")
        val common = contentSetting.getAsJsonObject("common")

        assertEquals("HTML_SOURCE", contentSetting["content_type"].asString)
        assertEquals("Title", contentSetting["content_name"].asString)
        assertEquals("https://news.example/1.jpg", contentSetting["preview_image"].asString)
        assertFalse(contentSetting.has("base64_file"))
        assertEquals("https://news.example/1", common["source"].asString)
        assertEquals("https://news.example/1.jpg", common["poster"].asString)
        assertEquals(
            JsonObject().apply {
                addProperty("section", "world")
                addProperty("articleId", "1")
            },
            json.getAsJsonObject("extra")
        )
        assertEquals("sharer-1", json["sharer_id"].asString)
    }
}
