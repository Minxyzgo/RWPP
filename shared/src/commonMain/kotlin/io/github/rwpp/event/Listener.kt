/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package io.github.rwpp.event

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import io.github.rwpp.event.EventPriority.*
import kotlin.coroutines.CoroutineContext

/**
 * 订阅者的状态
 */
enum class ListeningStatus {
    /**
     * 表示继续监听
     */
    LISTENING,

    /**
     * 表示已停止.
     */
    STOPPED
}

/**
 * 事件监听器.
 *
 * 取消监听: [complete]
 */
abstract class Listener<in E : Event> : CompletableJob by Job() {
    /**
     * 事件优先级
     * @see [EventPriority]
     */
    open val priority: EventPriority get() = NORMAL

    open val coroutineContext: CoroutineContext get() = this

    abstract suspend fun onEvent(event: E): ListeningStatus
}


/**
 * 事件优先级.
 *
 * 在广播时, 事件监听器的调用顺序为 (从左到右):
 * [HIGHEST] -> [HIGH] -> [NORMAL] -> [LOW] -> [LOWEST] -> [MONITOR]
 *
 * - 使用 [MONITOR] 优先级的监听器将会被**并行**调用.
 * - 使用其他优先级的监听器都将会**按顺序**调用.
 *   因此一个监听器的挂起可以阻塞事件处理过程而导致低优先级的监听器较晚处理.
 *
 * 当事件被 [拦截][Event.intercept] 后, 优先级较低 (靠右) 的监听器将不会被调用.
 */
enum class EventPriority {

    HIGHEST, HIGH, NORMAL, LOW, LOWEST,

    /**
     * 最低的优先级.
     *
     * 使用此优先级的监听器应遵循约束:
     * - 不 [拦截事件][Event.intercept]
     */
    MONITOR;

    companion object {
        val all = entries.toTypedArray()
    }
}