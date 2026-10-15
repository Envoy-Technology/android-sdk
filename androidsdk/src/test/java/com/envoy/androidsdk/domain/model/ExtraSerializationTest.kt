package com.envoy.androidsdk.domain.model

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ExtraSerializationTest {
    private val gson = Gson()

    private fun createLinkBody(extra: Map<String, String>?) = CreateLinkBody(
        contentSetting = ContentSetting(
            type = ContentType.HTML_SOURCE,
            name = "Name",
            description = "Description"
        ),
        extra = extra,
        sharerId = "sharer-1"
    )

    @Test
    fun createLinkExtra_isSentAsJsonObject() {
        val json = gson.toJsonTree(createLinkBody(mapOf("articleId" to "16208949"))).asJsonObject

        val expected = JsonObject().apply { addProperty("articleId", "16208949") }
        assertEquals(expected, json.get("extra"))
    }

    @Test
    fun createLinkWithoutExtra_doesNotSendIt() {
        val json = gson.toJsonTree(createLinkBody(null)).asJsonObject

        assertFalse(json.has("extra"))
    }

    @Test
    fun pixelEventExtra_isSentAsJsonObject() {
        val body = CreatePixelEventBody(eventName = "payment_success", extra = mapOf("campaign" to "spring"))

        val json = gson.toJsonTree(body).asJsonObject

        val expected = JsonObject().apply { addProperty("campaign", "spring") }
        assertEquals(expected, json.get("extra"))
    }
}
