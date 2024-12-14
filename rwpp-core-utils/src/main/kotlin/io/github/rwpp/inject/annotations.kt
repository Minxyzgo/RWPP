/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("unused")

package io.github.rwpp.inject

import kotlin.reflect.KClass

/**
 * 该注解用于标记一个顶层对象，用于其它 inject 注解识别注入类.
 *
 * @param clazz 需要注入的类.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class InjectClass(
    val clazz: KClass<*>,
)

/**
 * 该注解用于标记一个函数, 该函数将以指定方式[injectMode]注入到目标类的方法中.
 *
 * 该注解标记的函数必须位于被[InjectClass]标记的对象内.
 *
 * 若需要获得注入类的对象 (注意此时注入的方法应为非静态)，可以添加 receiver，且 receiver 类型必须与[InjectClass.clazz]一致.
 *
 * 以下为一个示例:
 * ```kotlin
 * @InjectClass(com.Example::class)
 * object A {
 *     @Inject("B")
 *     fun injectB() {
 *         // inject code here
 *     }
 *
 *     @Inject("A") // target method name is "A"
 *     fun com.Example.injectA() {
 *         // inject code here
 *     }
 * }
 *
 * ```
 *
 * @param method 要注入的目标方法名.
 * @param injectMode 注入模式, 默认为[InjectMode.Override], 即覆盖原有方法. 注意，若使用[InjectMode.InsertBefore], 则函数的返回类型必须为Any.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Inject(
    val method: String,
    val injectMode: InjectMode = InjectMode.Override,
)


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RedirectTo(
    val clazz: KClass<*>,
)
