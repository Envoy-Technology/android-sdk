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
