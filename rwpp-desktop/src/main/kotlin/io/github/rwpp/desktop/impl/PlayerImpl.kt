/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.data.PlayerData
import io.github.rwpp.net.Client

class PlayerImpl(
    internal val player: com.corrodinggames.rts.game.n,
    private val room: GameRoom
) : Player {
    override val connectHexId: String
        get() = player.O ?: ""
    override var spawnPoint: Int
        get() = player.k
        set(value) {
            if (room.isHost)
                player.f(value)
            else if (room.isHostServer) {
                GameEngine.B().bX.a(player, value, team)
            }
        }
    override var team: Int
        get() = player.r
        set(value) {
            if (room.isHost)
                player.r = value
            else if (room.isHostServer) GameEngine.B().bX.b(player, value)
        }
    override var name: String
        get() = player.v ?: ""
        set(value) {
            if (room.isHost) player.v = value
        }
    override val ping: String
        get() = player.z()
    override var startingUnit: Int
        get() = player.A ?: -1
        set(value) {
            if (room.isHost) player.A = if (value == -1) null else value
        }
    override var color: Int
        get() = player.C ?: -1
        set(value) {
            if (room.isHost) player.C = if (value == -1) null else value
        }
    override val isSpectator: Boolean
        get() = team == -3
    override val isAI: Boolean
        get() = player.w
    override var difficulty: Difficulty?
        get() = if(isAI) player.z?.let { Difficulty.entries[it + 2] } else null
        set(value) {
            if (room.isHost) player.z = value?.ordinal?.minus(2)
        }
    override val data: PlayerData = PlayerData()
    override val client: Client by lazy {
        ClientImpl(GameEngine.B().bX.c(player))
    }

    override fun applyConfigChange(
        spawnPoint: Int,
        team: Int,
        color: Int?,
        startingUnits: Int?,
        aiDifficulty: Difficulty?,
        changeTeamFromSpawn: Boolean
    ) {
        val B = GameEngine.B()
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
            player.u = false
            z = true
        } else {
            z = false
            i = this.team
            try {
                i = team - 1
            } catch(e: NumberFormatException) {
                e.printStackTrace()
            }
            player.u = true
        }
        if(this.team != i) {
            if(room.isHost) {
                z3 = true
            } else if(B.bX.H || B.bX.z == player) {
                z3 = true
            } else {
                // l.b("row.setOnClickListener", "Clicked but not server or proxy controller")
            }
        }
        try {
            if(player.k != intValue) {
                if(room.isHost) {
                    z3 = false
                    B.bX.a(player, intValue)
                    player.r = i
                } else if(B.bX.H || B.bX.z == player) {
                    z3 = false
                    var i2: Int = i
                    if(z) {
                        i2 = -1
                    }
                    B.bX.a(player, intValue, i2)
                } else {
                    // l.b("row.setOnClickListener", "Clicked but not server or proxy controller")
                }
            }
        } catch(e2: NumberFormatException) {
            e2.printStackTrace()
        }
        if(player.w) {
            val intValue2: Int = (aiDifficulty?.ordinal?.minus(2)) ?: -99
            valueOf3 = if(intValue2 == -99) {
                null
            } else {
                intValue2
            }
            if(player.z != valueOf3) {
                if(room.isHost) {
                    player.z = valueOf3
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
            if(player.A != valueOf) {
                if(room.isHost) {
                    player.A = valueOf
                } else {
                    // l.e("startingUnitOverride: not server or proxy controller")
                }
            }
        } else {
            player.A = null
        }

        val intValue4 = color
        // l.e("playerColor now: $intValue4")
        if(intValue4 != null) {
            if(intValue4 == -99) {
                valueOf2 = null
            } else {
                valueOf2 = intValue4
            }
            if(player.C != valueOf2) {
                if(room.isHost) {
                    player.C = valueOf2
                } else {
                    // l.e("colorOverride: not server or proxy controller")
                }
            }
        } else {
            player.C = null
        }


        if(z3) {
            if(room.isHost) {
                player.r = i
            } else if(z) {
                B.bX.b(player, -1)
            } else {
                B.bX.b(player, i)
            }
        }
        B.bX.f()
        B.bX.M()
    }
}