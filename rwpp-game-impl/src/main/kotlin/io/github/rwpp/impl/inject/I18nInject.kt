/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl.inject

import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.*
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

@InjectClass(com.corrodinggames.rts.gameFramework.h.a::class)
object I18nInject {

    @Inject("d", InjectMode.InsertBefore)
    fun onParseI18n(str: String) {
        when (str) {
            "gui.log.unitDamaged" -> LocalPlayerUnitDamagedEvent().broadcastIn()
            "gui.log.baseDamaged" -> LocalPlayerBaseDamagedEvent().broadcastIn()
            "gui.log.unitCreated" -> LocalPlayerUnitCreatedEvent().broadcastIn()
            "gui.log.buildingConstructed" -> LocalPlayerBuildingConstructedEvent().broadcastIn()
            "gui.log.upgradeCompleted" -> LocalPlayerUpgradedCompletedEvent().broadcastIn()
        }
    }
}