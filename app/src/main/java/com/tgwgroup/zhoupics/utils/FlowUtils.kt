package com.tgwgroup.zhoupics.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun <T> Flow<T>.collectIn(scope: CoroutineScope, block: suspend (T) -> Unit) {
    scope.launch {
        collect {
            block(it)
        }
    }
}