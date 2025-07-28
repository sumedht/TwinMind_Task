package com.sumedh.twinmind.app.util

sealed class Result<out T> {
    /**
     * Represents a successful result.
     * @param data The data returned from the successful operation.
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Represents a failed result.
     * @param exception The exception that caused the failure.
     */
    data class Error(val exception: Exception) : Result<Nothing>()
}