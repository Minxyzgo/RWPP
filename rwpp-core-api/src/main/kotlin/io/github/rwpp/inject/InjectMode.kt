/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.inject

import io.github.rwpp.inject.InjectMode.Override


/**
 * 注入模式，通常默认为[Override]
 */
enum class InjectMode {
    /**
     * 函数将运行在方法之前。若函数返回Unit, 则使用代理方法的返回值, 若不为Unit, 则中断方法进行并返回
     */
    InsertBefore,

    /**
     * 函数完全覆盖原方法。
     */
    Override,

    /**
     * 函数将运行在原方法之后。若函数返回Unit, 则使用原方法的返回值
     */
    InsertAfter
}
