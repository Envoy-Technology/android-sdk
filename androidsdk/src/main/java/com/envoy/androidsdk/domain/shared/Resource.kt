package com.envoy.androidsdk.domain.shared

sealed class Resource<out T>
class Loading<out T> : Resource<T>() {
    override fun equals(other: Any?): Boolean {
        return other is Loading<*>
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class Success<out T>(val value: T) : Resource<T>() {
    override fun equals(other: Any?): Boolean {
        return if (other is Success<*>) {
            value == other.value
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }
}

class Failure<out T>(val throwable: Throwable) : Resource<T>() {
    override fun equals(other: Any?): Boolean {
        return if (other is Failure<*>) {
            throwable::class.java == other.throwable::class.java && throwable.message == other.throwable.message
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return throwable.hashCode()
    }
}

fun <T, U> Resource<T>.map(transform: (T) -> U): Resource<U> {
    return when (this) {
        is Success -> Success(value = transform(value))
        is Failure -> Failure(throwable = throwable)
        is Loading -> Loading()
    }
}

inline fun <T> Resource<T>.onSuccess(block: (T) -> Unit): Resource<T> =
    apply {
        if (this is Success<T>) block(value)
    }

inline fun <T> Resource<T>.onFailure(block: (Throwable) -> Unit): Resource<T> =
    apply {
        if (this is Failure<T>) block(throwable)
    }

inline fun <T> Resource<T>.onLoading(block: () -> Unit): Resource<T> =
    apply {
        if (this is Loading<T>) block()
    }