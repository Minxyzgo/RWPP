/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.inject

/**
 * 当注入模式为[InjectMode.InsertBefore]有效，若注入的函数代码返回[InterruptResult]时，则中断方法继续运行，并返回给定结果。
 */
class InterruptResult(val result: Any? = Unit)