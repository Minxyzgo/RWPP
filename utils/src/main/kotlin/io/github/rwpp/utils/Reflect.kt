/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.utils

import kotlin.reflect.KClass

object Reflect {

    @Suppress("UNCHECKED_CAST")
    fun <T> get(any: Any, field: String): T? {
        return any::class.java.getDeclaredField(field).apply {
            isAccessible = true
        }.get(any) as T?
    }

    fun set(any: Any, field: String, value: Any?) {
        any::class.java.getDeclaredField(field).apply {
            isAccessible = true
        }.set(any, value)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T, K> call(
        any: T?,
        method: String,
        classes: List<KClass<*>>,
        args: List<Any?>
    ): K? {
        return T::class.java.getDeclaredMethod(
            method, *classes.map { it.java }.toTypedArray()
        ).apply {
            isAccessible = true
        }.invoke(any, *args.toTypedArray()) as K?
    }
}