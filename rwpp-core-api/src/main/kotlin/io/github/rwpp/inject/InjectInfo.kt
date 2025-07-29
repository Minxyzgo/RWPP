/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.inject

import kotlinx.serialization.Serializable

@Serializable
data class RootInfo(
    val injectInfos: Set<InjectInfo> = emptySet(),
    val setInterfaceOnInfos: Set<SetInterfaceOnInfo> = emptySet(),
    val redirectToInfos: Set<RedirectToInfo> = emptySet(),
    val redirectMethodInfos: Set<RedirectMethodInfo> = emptySet(),
)

@Serializable
data class InjectInfo(
    val className: String,
    val hasReceiver: Boolean,
    val methodName: String,
    val methodDesc: String,
    val path: String,
    val pathType: PathType,
    val returnClassIsVoid: Boolean,
    val injectMode: InjectMode,
) {
    fun signature() = "$className.$methodName$methodDesc"
}

@Serializable
data class SetInterfaceOnInfo(
    val interfaceName: String,
    val targetClassName: String,
    val newFields: List<Pair<String, String>>, // Pair<fieldName, fieldType>
    val accessors: List<Pair<String, String>>, // Pair<propertyName, fieldName>
    val hasSelfField: Boolean = false,
)

@Serializable
data class RedirectToInfo(
    val from: String,
    val to: String,
)

@Serializable
data class RedirectMethodInfo(
    val hasReceiver: Boolean,
    val className: String,
    val method: String,
    val methodDesc: String,
    val targetClassName: String,
    val targetMethod: String,
    val targetMethodDesc: String,
    val path: String,
    val pathType: PathType
) {
    fun signature() = "$className.$method$methodDesc:$targetClassName.$targetMethod$targetMethodDesc"
}

enum class PathType {
    JavaCode, Path
}