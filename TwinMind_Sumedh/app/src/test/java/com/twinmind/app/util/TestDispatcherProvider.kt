package com.twinmind.app.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherProvider(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) {
    val main: CoroutineDispatcher  = testDispatcher
    val io: CoroutineDispatcher    = testDispatcher
    val default: CoroutineDispatcher = testDispatcher
}