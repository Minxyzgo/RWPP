/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */


package io.github.rwpp.desktop.impl

import com.corrodinggames.rts.game.n
import io.github.rwpp.appKoin
import io.github.rwpp.core.Logic
import io.github.rwpp.desktop.GameEngine
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.data.PlayerData
import io.github.rwpp.game.data.PlayerStatisticsData
import io.github.rwpp.inject.NewField
import io.github.rwpp.inject.SetInterfaceOn
import io.github.rwpp.logger
import io.github.rwpp.net.Client
import kotlin.math.roundToInt

@SetInterfaceOn([n::class])
interface PlayerImpl : Player {
    private val room: GameRoom
        get() = appKoin.get<Game>().gameRoom

    val self: n

    @NewField
    var _connectHexId: String?

    @NewField
    var _data: PlayerData?

    override val connectHexId: String
        get() = self.O ?: run {
            _connectHexId = if (_connectHexId == null || _connectHexId == "0") {
                Logic.getNextPlayerId().toString()
            } else {
                _connectHexId
            }
            _connectHexId!!
        }
    override var spawnPoint: Int
        get() = self.k
        set(value) {
            if (room.isHost)
                self.f(value)
            else if (room.isHostServer) {
                GameEngine.B().bX.a(self, value, team)
            }
        }
    override var team: Int
        get() = self.r
        set(value) {
            if (room.isHost)
                self.r = value
            else if (room.isHostServer) GameEngine.B().bX.b(self, value)
        }
    override var name: String
        get() = self.v ?: ""
        set(value) {
            if (room.isHost) self.v = value
        }
    override val ping: String
        get() = self.z()
    override var startingUnit: Int
        get() = self.A ?: -1
        set(value) {
            if (room.isHost) self.A = if (value == -1) null else value
        }
    override var color: Int
        get() = self.C ?: -1
        set(value) {
            if (room.isHost) self.C = if (value == -1) null else value
        }
    override val isSpectator: Boolean
        get() = team == -3
    override val isAI: Boolean
        get() = self.w
    override var difficulty: Int?
        get() = if(isAI) self.z else null
        set(value) {
            if (room.isHost) self.z = value
        }
    override var credits: Int
        //4000.0d
        get() = self.o.roundToInt()
        set(value) { self.o = value.toDouble() }
    override val statisticsData: PlayerStatisticsData
        get() = with(GameEngine.B().bY.a(self)) {
            PlayerStatisticsData(c, d, e, f, g, h)
        }
    override val income: Int
        get() = self.v()
    override val isDefeated: Boolean
        get() = self.F || self.G
    override val isWipedOut: Boolean
        get() = self.G
    override val client: Client?
        get() = GameEngine.B().bX.c(self) as Client?

    override val data: PlayerData
        get() = _data ?: run {
            _data = PlayerData()
            _data!!
        }

    override fun applyConfigChange(
        spawnPoint: Int,
        team: Int,
        color: Int?,
        startingUnits: Int?,
        aiDifficulty: Int?,
        autoTeamMode: Boolean
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
            if(intValue > n.c - 1) {
                intValue = n.c - 1
            }
        }
        var z3 = false
        if(z2) {
            i = -3
            z = true
        } else if(autoTeamMode) {
            i = intValue % 2

            val p = com.corrodinggames.rts.game.n.k(spawnPoint)
            if (p != null) {
                i = p.r
            } else if (room.teamMode != null) {
                i = room.teamMode!!.autoTeamAssign(room, spawnPoint, this)
            }

            self.u = false
            z = true
        } else {
            z = false
            i = team - 1
            self.u = true
        }
        if(this.team != i) {
            if(room.isHost) {
                z3 = true
            } else if(B.bX.H || B.bX.z == self) {
                z3 = true
            } else {
                // l.b("row.setOnClickListener", "Clicked but not server or proxy controller")
            }
        }
        try {
            if(self.k != intValue) {
                if(room.isHost) {
                    z3 = false
                    B.bX.a(self, intValue)
                    self.r = i
                } else if(B.bX.H || B.bX.z == self) {
                    z3 = false
                    var i2: Int = i
                    if(z) {
                        i2 = -1
                    }
                    B.bX.a(self, intValue, i2)
                } else {
                    // l.b("row.setOnClickListener", "Clicked but not server or proxy controller")
                }
            }
        } catch(e2: NumberFormatException) {
            logger.error(e2.stackTraceToString())
        }
        if(self.w) {
            val intValue2: Int = (aiDifficulty) ?: -99
            valueOf3 = if(intValue2 == -99) {
                null
            } else {
                intValue2
            }
            if(self.z != valueOf3) {
                if(room.isHost) {
                    self.z = valueOf3
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
            if(self.A != valueOf) {
                if(room.isHost) {
                    self.A = valueOf
                } else {
                    // l.e("startingUnitOverride: not server or proxy controller")
                }
            }
        } else {
            self.A = null
        }

        val intValue4 = color
        // l.e("playerColor now: $intValue4")
        if(intValue4 != null) {
            if(intValue4 == -99) {
                valueOf2 = null
            } else {
                valueOf2 = intValue4
            }
            if(self.C != valueOf2) {
                if(room.isHost) {
                    self.C = valueOf2
                } else {
                    // l.e("colorOverride: not server or proxy controller")
                }
            }
        } else {
            self.C = null
        }


        if(z3) {
            if(room.isHost) {
                self.r = i
            } else if(z) {
                B.bX.b(self, -1)
            } else {
                B.bX.b(self, i)
            }
        }
        B.bX.f()
        B.bX.M()
    }
}