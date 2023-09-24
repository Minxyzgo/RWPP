/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.base.Difficulty

class PlayerImpl(
    internal val player: PlayerInternal,
    private val room: GameRoom
) : Player {
    override val connectHexId: String
        get() = player.S ?: ""
    override val spawnPoint: Int
        get() = player.l
    override val team: Int
        get() = player.s
    override val name: String
        get() = player.w
    override val ping: String
        get() {
            val t = player.t()
            return if(t == -99) {
                "HOST"
            } else if(isAI) {
                "-"
            } else if(t == -1) {
                "N/A"
            } else if(t == -2) {
                "-"
            } else {
                if(player.T == 1) "$t (HOST)" else java.lang.String.valueOf(t)
            }
        }
    override val startingUnit: Int
        get() = player.B
    override val color: Int
        get() = player.D
    override val isSpectator: Boolean
        get() = team == -3
    override val isAI: Boolean
        get() = player.x
    override val difficulty: Difficulty?
        get() = if(isAI) player.y.let { Difficulty.entries[it + 2] } else null

    private val aeMethod = com.corrodinggames.rts.gameFramework.j.ae::class.java.getDeclaredMethod("P")
        .apply { isAccessible = true }
    override fun applyConfigChange(
        spawnPoint: Int,
        team: Int,
        color: Int?,
        startingUnits: Int?,
        aiDifficulty: Difficulty?,
        changeTeamFromSpawn: Boolean
    ) {
        val t = GameEngine.t()
        var intValue: Int = spawnPoint
        var z = false
        var i = 0
        var z2 = false
        val valueOf: Int?
        val valueOf2: Int?
        val valueOf3: Int?
        if(intValue == -3) {
            z2 = true
        } else {
            if(intValue < 0) {
                intValue = 1
            }
            if(intValue > PlayerInternal.c - 1) {
                intValue = PlayerInternal.c - 1
            }
        }
        var z3 = false
        if(z2) {
            i = -3
            z = true
        } else if(changeTeamFromSpawn) {
            i = intValue % 2
            //player.u = false
            z = true
        } else {
            z = false
            i = this.team
            try {
                i = team - 1
            } catch(e: NumberFormatException) {
                e.printStackTrace()
            }
            //player.u = true
        }
        if(this.team != i) {
            if(room.isHost) {
                z3 = true
            } else if(room.isHost || room.localPlayer == this) {
                z3 = true
            } else {
                // l.b("row.setOnClickListener", "Clicked but not server or proxy controller")
            }
        }
        try {
            if(this.spawnPoint != intValue) {
                if(room.isHost) {
                    z3 = false
                    t.bU.a(player, intValue)
                    player.l = i
                } else if(room.isHost || room.localPlayer == this) {
                    z3 = false
                    var i2: Int = i
                    if(z) {
                        i2 = -1
                    }
                    t.bU.a(player, intValue)
                } else {
                    // l.b("row.setOnClickListener", "Clicked but not server or proxy controller")
                }
            }
        } catch(e2: NumberFormatException) {
            e2.printStackTrace()
        }
        if(isAI) {
            val intValue2: Int = (aiDifficulty?.ordinal?.minus(2)) ?: -99
            valueOf3 = if(intValue2 == -99) {
                null
            } else {
                intValue2
            }
            if(player.y != valueOf3) {
                if(room.isHost) {
                    player.y = intValue2
                } else {
                    //l.e("aiDifficultyOverride: not server or proxy controller")
                }
            }
        }
        val intValue3 = startingUnits
        // l.e("startingUnits now: $intValue3")
        if(intValue3 != null) {
            if(intValue3 == -99) {
                valueOf = null
            } else {
                valueOf = intValue3
            }
            if(player.B != valueOf) {
                if(room.isHost) {
                    player.B = valueOf
                } else {
                    // l.e("startingUnitOverride: not server or proxy controller")
                }
            }
        } else {
            player.B = null
        }

        val intValue4 = color
        // l.e("playerColor now: $intValue4")
        if(intValue4 != null) {
            if(intValue4 == -99) {
                valueOf2 = null
            } else {
                valueOf2 = intValue4
            }
            if(player.D != valueOf2) {
                if(room.isHost) {
                    player.D = valueOf2
                } else {
                    // l.e("colorOverride: not server or proxy controller")
                }
            }
        }

        if(z3) {
            if(room.isHost) {
                player.r = i
            } else if(z) {
                t.bU.b(player, -1)
            } else {
                t.bU.b(player, i)
            }
        }

        t.bU.b()
        aeMethod.invoke(t.bU)
    }
}