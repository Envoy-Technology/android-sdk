package com.envoy.androidsdk.domain.shared

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import retrofit2.Response
import kotlin.coroutines.CoroutineContext

private const val GENERIC_SERVER_ERROR = "Something went wrong, please try again."
private const val PARSING_SERVER_ERROR = "The response could not be parsed"
private const val SYNTAX_SERVER_ERROR = "The response doesn't have a valid format"

internal fun ResponseBody.getParsedError(): String =
    try {
        val root = JsonParser.parseString(string())
        val detail = (root as? JsonObject)?.get("detail")
        when {
            detail == null -> GENERIC_SERVER_ERROR
            detail.isJsonPrimitive && detail.asJsonPrimitive.isString -> detail.asString
            detail.isJsonArray -> {
                val first = detail.asJsonArray.firstOrNull() as? JsonObject
                first?.get("msg")?.asString ?: GENERIC_SERVER_ERROR
            }
            else -> GENERIC_SERVER_ERROR
        }
    } catch (ex: JsonSyntaxException) {
        SYNTAX_SERVER_ERROR
    } catch (ex: JsonParseException) {
        PARSING_SERVER_ERROR
    }

@Suppress("TooGenericExceptionCaught")
internal fun <T> performRequest(
    block: suspend () -> Response<T>,
    coroutineContext: CoroutineContext
): Flow<Resource<T>> = flow<Resource<T>> {
    emit(Loading())
    try {
        val response = block()
        if (response.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            emit(Success(response.body() ?: Unit as T))
        } else {
            emit(Failure(Throwable(message = response.errorBody()?.getParsedError() ?: "Request failed")))
        }
    } catch (ex: Exception) {
        emit(Failure(ex))
    }
}.flowOn(coroutineContext)
