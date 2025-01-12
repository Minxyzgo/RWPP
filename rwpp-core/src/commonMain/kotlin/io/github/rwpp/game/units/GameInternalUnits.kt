/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.units

/**
 * @date 2023/8/19 12:06
 * @author RW-HPS/Dr
 */
enum class GameInternalUnits {
    extractor,
    landFactory,
    airFactory,
    seaFactory,
    commandCenter,
    turret,
    antiAirTurret,
    builder,
    tank,
    hoverTank,
    artillery,
    helicopter,
    airShip,
    gunShip,
    missileShip,
    gunBoat,
    megaTank,
    laserTank,
    hovercraft,
    ladybug,
    battleShip,
    tankDestroyer,
    heavyTank,
    heavyHoverTank,
    laserDefence,
    dropship,
    tree,
    repairbay,
    NukeLaucher,
    AntiNukeLaucher,
    mammothTank,
    experimentalTank,
    experimentalLandFactory,
    crystalResource,
    wall_v,
    fabricator,
    attackSubmarine,
    builderShip,
    amphibiousJet,
    supplyDepot,
    experimentalHoverTank,
    turret_artillery,
    turret_flamethrower,
    fogRevealer,
    spreadingFire,
    antiAirTurretT2,
    turretT2,
    turretT3,
    damagingBorder,
    zoneMarker,
    editorOrBuilder,
    UNKNOWN;
    //modularSpider

    companion object {
        private val unitMap = mutableMapOf<Int, GameInternalUnits>()

        init {
            entries.forEach {
                if (unitMap.containsKey(it.ordinal)) {
                    throw RuntimeException("[GameUnitType -> GameUnits]")
                }
                unitMap[it.ordinal] = it
            }
        }

        // 进行全匹配 查看是否在游戏内置列表中
        fun from(type: String?): GameInternalUnits? = entries.find { it.name == type || it.name.lowercase() == type?.lowercase() }

        fun from(type: Int): GameInternalUnits = unitMap[type] ?: UNKNOWN
    }
}