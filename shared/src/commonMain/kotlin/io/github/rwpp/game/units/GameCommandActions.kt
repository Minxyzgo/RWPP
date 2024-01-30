/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.units

/**
 * @date 2023/8/19 11:02
 * @author RW-HPS/Dr
 */
enum class GameCommandActions {
    MOVE,
    ATTACK,
    BUILD,
    REPAIR,
    LOADINTO,
    UNLOADAT,
    RECLAIM,
    ATTACKMOVE,
    LOADUP,
    PATROL,
    GUARD,
    GUARDAT,
    TOUCHTARGET,
    FOLLOW,
    TRIGGERACTION,
    TRIGGERACTIONWHENINRANGE,
    SETPASSIVETARGET,
    UNKNOWN;

    companion object {
        private val actionMap = mutableMapOf<Int, GameCommandActions>()

        init {
            GameCommandActions.values().forEach {
                if (actionMap.containsKey(it.ordinal)) {
                    throw RuntimeException("[GameUnitType -> GameActions]")
                }
                actionMap[it.ordinal] = it
            }
        }

        // 进行全匹配 查看是否在游戏内置列表中
        fun from(type: String?): GameCommandActions? = entries.find { it.name == type || it.name.lowercase() == type?.lowercase() }

        fun from(type: Int): GameCommandActions = actionMap[type] ?: UNKNOWN
    }
}