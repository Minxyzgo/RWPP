package io.github.rwpp.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
inline fun <T : Event> EventChannel<T>.onDispose(crossinline listenerProvider: EventChannel<T>.() -> Listener<T>) {
    DisposableEffect(Unit) {
        val listener = listenerProvider()
        onDispose {
            unregisterListener(listener)
        }
    }
}