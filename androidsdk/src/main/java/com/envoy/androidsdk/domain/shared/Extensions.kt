package com.envoy.androidsdk.domain.shared

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import retrofit2.Response

private const val GENERIC_SERVER_ERROR = "Something went wrong, please try again."
private const val PARSING_SERVER_ERROR = "The response could not be parsed"
private const val SYNTAX_SERVER_ERROR = "The response doesn't have a valid format"

internal fun ResponseBody.getParsedError(): String =
    try {
        val apiError = Gson().fromJson(string(), ApiError::class.java)
        apiError?.details?.firstOrNull()?.message ?: GENERIC_SERVER_ERROR
    } catch (ex: JsonParseException) {
        PARSING_SERVER_ERROR
    } catch (ex: JsonSyntaxException) {
        SYNTAX_SERVER_ERROR
    }

@Suppress("TooGenericExceptionCaught")
internal fun <T> performRequest(
    block: suspend () -> Response<T>,
    coroutineContext: CoroutineContext,
): Flow<Resource<T>> = flow<Resource<T>> {
    emit(Loading())
    try {
        val response = block()
        response.body()?.let {
            emit(Success(it))
        } ?: run {
            emit(Failure(Throwable(message = response.errorBody()?.getParsedError() ?: "")))
        }
    } catch (ex: Exception) {
        emit(Failure(ex))
    }
}.flowOn(coroutineContext)