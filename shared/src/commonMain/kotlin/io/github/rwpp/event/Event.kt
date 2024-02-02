/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.event

import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmField
import kotlin.jvm.Volatile

/**
 * 表示一个事件.
 *
 * 实现时应继承 [AbstractEvent] 而不要直接实现 [Event]. 否则将无法广播.
 *
 * ## 广播事件
 *
 * 使用 [Event.broadcast]
 *
 * Kotlin:
 * ```
 * val event: Event = ...
 * event.broadcast()
 * ```
 *
 * ## 监听事件
 *
 * 参阅 [EventChannel].
 */
interface Event {
    /**
     * 事件是否已被拦截.
     *
     * 所有事件都可以被拦截, 拦截后低优先级的监听器将不会处理到这个事件.
     *
     * @see intercept 拦截事件
     */
    val isIntercepted: Boolean

    /**
     * 拦截这个事件
     *
     * 当事件被 [拦截][Event.intercept] 后, 优先级较低 (靠右) 的监听器将不会被调用.
     *
     * 优先级为 [EventPriority.MONITOR] 的监听器不应该调用这个函数.
     *
     * @see EventPriority 查看优先级相关信息
     */
    fun intercept()
}

/**
 * 所有实现了 [Event] 接口的类都应该继承的父类.
 *
 * 在使用事件时应使用类型 [Event]. 在实现自定义事件时应继承 [AbstractEvent].
 */
abstract class AbstractEvent : Event, BroadcastControllable {
    @Suppress("PropertyName")
    @JvmField
    @Volatile
    var _intercepted: Boolean = false

    @Suppress("PropertyName")
    @Volatile
    var _cancelled: Boolean = false
        internal set

    // 实现 Event
    /**
     * @see Event.isIntercepted
     */
    override val isIntercepted: Boolean
        get() {
            return _intercepted
        }

    /**
     * @see Event.intercept
     */
    override fun intercept() {
        _intercepted = true
    }
}

/**
 * 广播一个事件的途径. 协程将挂起直到事件处理完成
 */
suspend fun <E : Event> E.broadcast(): E {
    GlobalEventChannel.broadcast(this)
    return this
}

/**
 * 广播一个事件的途径. 通过给定的上下文启动新协程来处理事件
 *
 * @param coroutineContext 需要给定的协程上下文. 默认为[EmptyCoroutineContext]
 */
@Suppress("unused")
fun <E : Event> E.broadCastIn(coroutineContext: CoroutineContext = EmptyCoroutineContext): E {
    GlobalEventChannel.launch(coroutineContext) { broadcast() }
    return this
}

/**
 * 可控制是否需要广播这个事件
 *
 * 如果不需要，则在第一次被任意接收器接受后中断
 */
interface BroadcastControllable : Event {
    /**
     * 返回 `false` 时将不会广播这个事件.
     */
    val shouldBroadcast: Boolean
        get() = true
}
