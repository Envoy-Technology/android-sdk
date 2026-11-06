package com.envoy.androidsdk.domain.model

import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ContentProtectionSerializationTest {

    private fun body(contentProtection: ContentProtection?) = CreateLinkBody(
        contentSetting = ContentSetting(
            type = ContentType.VIDEO,
            name = "Name",
            description = "Description",
            commonData = CommonData(source = "https://cdn.example/s.mpd", isRedirect = false, poster = null),
            contentProtection = contentProtection
        ),
        sharerId = "sharer-1"
    )

    @Test
    fun isSentInTheShakaShape() {
        val protection = ContentProtection(
            sources = mapOf(KeySystem.FAIR_PLAY to "https://cdn.example/s.m3u8"),
            drm = DrmConfig(
                servers = mapOf(KeySystem.WIDEVINE to "https://license.example/wv"),
                advanced = mapOf(
                    KeySystem.FAIR_PLAY to AdvancedDrmConfig(
                        serverCertificate = "Y2VydA==",
                        serverCertificateUri = "https://license.example/fps.cer",
                        headers = mapOf("X-Drm-Token" to "t")
                    )
                )
            ),
            fairPlayProvider = FairPlayProvider.EZDRM
        )

        val sent = Gson().toJsonTree(body(protection)).asJsonObject
            .getAsJsonObject("content_setting")
            .getAsJsonObject("content_protection")

        assertEquals(
            JsonParser.parseString(
                """
                {
                  "sources": {"com.apple.fps": "https://cdn.example/s.m3u8"},
                  "drm": {
                    "servers": {"com.widevine.alpha": "https://license.example/wv"},
                    "advanced": {
                      "com.apple.fps": {
                        "serverCertificate": "Y2VydA==",
                        "serverCertificateUri": "https://license.example/fps.cer",
                        "headers": {"X-Drm-Token": "t"}
                      }
                    }
                  },
                  "fair_play_provider": "ezdrm"
                }
                """
            ),
            sent
        )
    }

    @Test
    fun leavesOutWhatIsNotSet() {
        val sent = Gson().toJsonTree(body(ContentProtection(drm = DrmConfig(servers = emptyMap())))).asJsonObject
            .getAsJsonObject("content_setting")
            .getAsJsonObject("content_protection")

        assertEquals(JsonParser.parseString("""{"drm": {"servers": {}}}"""), sent)
    }

    @Test
    fun noContentProtection_isNotSent() {
        val contentSetting = Gson().toJsonTree(body(null)).asJsonObject.getAsJsonObject("content_setting")

        assertFalse(contentSetting.has("content_protection"))
    }
}
