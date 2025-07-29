/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.rwpp.event

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

/**
 *
 * 广播事件, 使用 [Event.broadcast] 或 [GlobalEventChannel.broadcast].
 *
 * [GlobalEventChannel.filter] 获取子事件通道.
 *
 * ### 创建事件监听
 * - [GlobalEventChannel.receiveGlobal] 监听并等待直到一个事件返回.
 * - [GlobalEventChannel.subscribeGlobalAlways] 创建一个总是监听指定事件的事件监听器.
 * - [GlobalEventChannel.subscribeGlobalOnce] 创建一个只监听单次的指定事件监听器.*
 * - [GlobalEventChannel.receive] 监听并等待直到一个任意事件返回.
 * - [GlobalEventChannel.subscribeAlways] 创建一个总是监听所有事件的事件监听器.
 * - [GlobalEventChannel.subscribeOnce] 创建一个只监听单次的任意事件监听器.
 */
sealed class GlobalEventChannel(coroutineScope: CoroutineScope)
    : EventChannel<Event>(coroutineScope) {
    private val channels = ConcurrentHashMap<Class<out Event>, EventChannel<Event>>()
    override val _events: MutableSharedFlow<Event> = MutableSharedFlow<Event>().apply {
        onEach { event ->
            launch {
                runListener(event)
                channels[event::class.java]?._events?.emit(event) ?: event.job.complete()
            }
        }.catch {
            errorHandler(it)
        }.launchIn(this@GlobalEventChannel)
    }

    /**
     * 发送一个事件.
     */
    suspend fun broadcast(e: Event) {
        _events.emit(e)
        e.job.join()
    }

    /**
     * 获取指定事件的通道
     *
     * 与[subscribeGlobalAlways], [subscribeGlobalOnce]等不同的是，在该channel内监听优先级较低，且不会监听子类事件
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Event> filter(eventClass: KClass<T>): EventChannel<T> {
        return channels.getOrPut(eventClass.java) { EventChannel(coroutineScope) } as EventChannel<T>
    }

    /**
     * 监听事件，将等待并直到有一个事件 [T] 或其子类事件返回，类似[Channel.receive].
     *
     * @param timeout 设置该协程超时所需的时间，小于0则不设置，超时则返回null.
     * @param coroutineContext 在 [coroutineContext] 的基础上, 给事件监听协程的额外的 [CoroutineContext]
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any> receiveGlobal(
        eventClass: KClass<T>,
        timeout: Long = -1,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
    ): T? = withContext(this.coroutineContext) {
        var result: T? = null
        suspend fun collect() {
            launch(coroutineContext) {
                _events
                    .filter { !it.isIntercepted && eventClass.java.isAssignableFrom(it::class.java) }
                    .collectLatest {
                        result = it as T
                        cancel()
                    }
            }.join()
        }
        if(timeout > 0) withTimeoutOrNull(timeout) { collect() } else collect()
        result
    }


    /**
     * 创建一个事件监听器, 监听事件通道中所有 [T] 及其子类事件.
     * 每当 [事件广播][Event.broadcast] 时, [handler] 都会被执行.
     *
     * 可在任意时候通过 [Listener.complete] 来主动停止监听.
     *
     * @param coroutineContext 在 [coroutineContext] 的基础上, 给事件监听协程的额外的 [CoroutineContext]
     * @param priority 处理优先级, 优先级高的先执行
     *
     * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [handler]
     *
     * @see subscribe 获取更多说明
     */
    inline fun <reified T : Event> subscribeGlobalAlways(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        priority: EventPriority = EventPriority.NORMAL,
        crossinline handler: suspend (T) -> Unit,
    ): Listener<T> = registerListener(coroutineContext, priority, ListeningStatus.LISTENING) {
        if(T::class.java.isAssignableFrom(it::class.java)) handler(it as T)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> subscribeGlobalAlways(
        clazz: Class<T>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        priority: EventPriority = EventPriority.NORMAL,
        handler: suspend (T) -> Unit,
    ): Listener<T> = registerListener(coroutineContext, priority, ListeningStatus.LISTENING) {
        if(clazz.isAssignableFrom(it::class.java)) handler(it as T)
    }


    /**
     * 创建一个事件监听器, 监听事件通道中所有 [T] 及其子类事件, 只监听一次.
     * 当 [事件广播][Event.broadcast] 时, [handler] 会被执行.
     *
     * 可在任意时候通过 [Listener.complete] 来主动停止监听.
     *
     * @param coroutineContext 在 [coroutineContext] 的基础上, 给事件监听协程的额外的 [CoroutineContext]
     * @param priority 处理优先级, 优先级高的先执行
     *
     * @see subscribe 获取更多说明
     */
    inline fun <reified T : Event> subscribeGlobalOnce(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        priority: EventPriority = EventPriority.NORMAL,
        crossinline handler: suspend (T) -> Unit,
    ): Listener<T> = registerListener(coroutineContext, priority, ListeningStatus.STOPPED) {
        if(T::class.java.isAssignableFrom(it::class.java)) handler(it as T)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> subscribeGlobalOnce(
        clazz: Class<T>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        priority: EventPriority = EventPriority.NORMAL,
        handler: suspend (T) -> Unit,
    ): Listener<T> = registerListener(coroutineContext, priority, ListeningStatus.STOPPED) {
        if(clazz.isAssignableFrom(it::class.java)) handler(it as T)
    }

    companion object : GlobalEventChannel(CoroutineScope(SupervisorJob()))
}