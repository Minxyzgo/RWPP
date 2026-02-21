/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject


//import io.github.rwpp.appKoin
//import io.github.rwpp.config.Settings
//import io.github.rwpp.core.Logic
//import io.github.rwpp.desktop.GameEngine
//import io.github.rwpp.game.units.GameUnit
//import io.github.rwpp.inject.Inject
//import io.github.rwpp.inject.InjectClass
//import io.github.rwpp.inject.InjectMode
//import io.github.rwpp.inject.InterruptResult
//
//@InjectClass(com.corrodinggames.rts.gameFramework.f.g::class)
//object GuiInject {
//    val settings = appKoin.get<Settings>()
//    @Suppress("UNCHECKED_CAST")
//    @Inject("a", InjectMode.InsertBefore)
//    fun com.corrodinggames.rts.gameFramework.f.g.onAddSelectedUnits(command: com.corrodinggames.rts.gameFramework.e): Any? {
//        return if (settings.pathfindingOptimization && command.j?.d() == com.corrodinggames.rts.game.units.av.a) {
//            val selectedUnits = GameEngine.B().bS.bZ
//            val x = command.j.g()
//            val y = command.j.h()
//            val allCommandPackets = mutableListOf(command)
//            if (selectedUnits.size <= 9) {
//                repeat(9.coerceAtMost(selectedUnits.size) - 1) {
//                    allCommandPackets.add(this.x().apply {
//                        h = true
//                        a(x, y)
//                    })
//                }
//                allCommandPackets.forEachIndexed { i, command ->
//                    command.a(selectedUnits[i] as com.corrodinggames.rts.game.units.y)
//                }
//            } else {
//                val allParts = Logic.onPathfindingOptimization(x, y, selectedUnits as List<GameUnit>)
//                repeat(allParts.size - 1) { i ->
//                    allCommandPackets.add(this.x().apply {
//                        h = true
//                        a(allParts[i].first.toFloat(), allParts[i].second.toFloat())
//                    })
//                }
//                allCommandPackets.forEachIndexed { i, command ->
//                    allParts[i].third.forEach { unit -> command.a(unit as com.corrodinggames.rts.game.units.y) }
//                }
//            }
//            InterruptResult()
//        } else {
//            Unit
//        }
//    }
//}