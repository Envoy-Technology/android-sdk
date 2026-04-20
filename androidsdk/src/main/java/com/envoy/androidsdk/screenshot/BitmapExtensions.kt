package com.envoy.androidsdk.screenshot

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Converts a Bitmap to a Base64 encoded string.
 *
 * @param format The compression format (default: PNG)
 * @param quality The compression quality (0-100, default: 100)
 * @return Base64 encoded string of the bitmap
 */
fun Bitmap.toBase64(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(format, quality, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

/**
 * Converts a Bitmap to a Base64 encoded string with data URI prefix.
 * Useful for embedding directly in HTML or sending to APIs that expect data URIs.
 *
 * @param format The compression format (default: PNG)
 * @param quality The compression quality (0-100, default: 100)
 * @return Base64 encoded string with data URI prefix (e.g., "data:image/png;base64,...")
 */
fun Bitmap.toBase64DataUri(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): String {
    val base64 = toBase64(format, quality)
    val mimeType = when (format) {
        Bitmap.CompressFormat.JPEG -> "image/jpeg"
        Bitmap.CompressFormat.PNG -> "image/png"
        Bitmap.CompressFormat.WEBP,
        Bitmap.CompressFormat.WEBP_LOSSY,
        Bitmap.CompressFormat.WEBP_LOSSLESS -> "image/webp"
        else -> "image/png"
    }
    return "data:$mimeType;base64,$base64"
}
