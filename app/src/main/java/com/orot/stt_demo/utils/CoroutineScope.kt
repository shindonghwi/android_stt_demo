package com.orot.stt_demo.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

inline fun coroutineScopeOnMain(
    initDelay: Long = 0,
    crossinline body: suspend CoroutineScope.() -> Unit
) = CoroutineScope(Dispatchers.Main).launch {
    delay(initDelay)
    body()
}

inline fun coroutineScopeOnIO(
    initDelay: Long = 0,
    crossinline body: suspend CoroutineScope.() -> Unit
) = CoroutineScope(Dispatchers.IO).launch {
    delay(initDelay)
    body()
}

inline fun coroutineScopeOnDefault(
    initDelay: Long = 0,
    crossinline body: suspend CoroutineScope.() -> Unit,
) = CoroutineScope(Dispatchers.Default).launch {
    delay(initDelay)
    body()
}