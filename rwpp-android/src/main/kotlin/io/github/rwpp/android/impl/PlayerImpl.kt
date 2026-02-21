/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import com.corrodinggames.rts.game.p
import io.github.rwpp.appKoin
import io.github.rwpp.core.Logic
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.data.PlayerData
import io.github.rwpp.game.data.PlayerStatisticsData
import io.github.rwpp.inject.NewField
import io.github.rwpp.inject.SetInterfaceOn
import io.github.rwpp.net.Client
import kotlin.math.roundToInt

@SetInterfaceOn([PlayerInternal::class])
interface PlayerImpl : Player {
    private val room: GameRoom
        get() = appKoin.get<Game>().gameRoom

    val self: PlayerInternal

    @NewField
    var _connectHexId: String?

    @NewField
    var _data: PlayerData?

    override val connectHexId: String
        get() = self.S ?: run {
            _connectHexId = if (_connectHexId == null || _connectHexId == "0") {
                Logic.getNextPlayerId().toString()
            } else {
                _connectHexId
            }
            _connectHexId!!
        }
    override var spawnPoint: Int
        get() = self.l
        set(value) {
            applyConfigChange(spawnPoint = value)
        }
    override var team: Int
        get() = self.s
        set(value) {
            if (room.isHost) self.s = value
            else if (room.isHostServer) {
                val t = GameEngine.t()
                t.bU.b(self, value)
            }
        }
    override var name: String
        get() = self.w ?: ""
        set(value) {
            if (room.isHost) self.w = value
        }
    override val ping: String
        get() {
            val t = self.t()
            return if(t == -99) {
                "HOST"
            } else if(isAI) {
                "-"
            } else if(t == -1) {
                "N/A"
            } else if(t == -2) {
                "-"
            } else {
                if(self.T == 1) "$t (HOST)" else java.lang.String.valueOf(t)
            }
        }
    override var startingUnit: Int
        get() = self.B ?: -1
        set(value) {
            if (room.isHost) self.B = if (value == -1) null else value
        }
    override var color: Int
        get() = self.D ?: -1
        set(value) {
            if (room.isHost) self.D = if (value == -1) null else value
        }
    override val isSpectator: Boolean
        get() = team == -3
    override val isAI: Boolean
        get() = self.x
    override var difficulty: Int?
        get() = if(isAI) self.y else null
        set(value) { if(room.isHost) self.y = value!! }

    override var credits: Int
        get() = self.p.roundToInt()
        set(value) { self.p = value.toDouble() }
    override val statisticsData: PlayerStatisticsData
        get() = with(GameEngine.t().bV.a(self)) {
            PlayerStatisticsData(c, d, e, f, g, h)
        }
    override val income: Int
        get() = self.q()
    override val isDefeated: Boolean
        get() = self.I || self.J
    override val isWipedOut: Boolean
        get() = self.J
    override val data: PlayerData
        get() = _data ?: run {
            _data = PlayerData()
            _data!!
        }
    override val client: Client?
        get() = GameEngine.t().bU.c(self) as? Client
    override val pingNumber: Int
        get() = self.t()


    override fun applyConfigChange(
        spawnPoint: Int,
        team: Int,
        color: Int?,
        startingUnits: Int?,
        aiDifficulty: Int?,
        autoTeamMode: Boolean
    ) {
        val t = GameEngine.t()
        var bl: Boolean
        var n: Int
        var n2: Int
        var n3: Int
        var n4: Int
        run block31@{
            run block29@{
                run block30@{
                    n4 = 1;
                    n3 = (aiDifficulty) ?: -99
                    //k.d("newAiDifficultyValue:".concat(String.valueOf(n3)));
                    if(t.bU.D) {
                        self.A = if(n3 == -99) null else Integer.valueOf(n3);
                    }
                    n3 = startingUnits ?: -99
                    //k.d("startingUnits:" + this.c);
                    if(t.bU.D) {
                        self.B = if(n3 == -99) null else Integer.valueOf(n3);
                    }
                    n3 = color ?: -99
                    //k.d("newPlayerColorValue:".concat(String.valueOf(n3)));
                    if(t.bU.D) {
                        self.D = if(n3 == -99) null else Integer.valueOf(n3);
                    }
                    n3 = spawnPoint
                    if(n3 == -3 || n3 > p.c - 1) {
                        n2 = -3;
                        n3 = 1;
                    } else {
                        n2 = n3;
                        if(n3 < 0) {
                            n2 = 0;
                        }
                        if(n2 > p.c - 1) {
                            n2 = p.c - 1;
                            n3 = 0;
                        } else {
                            n3 = 0;
                        }
                    }
                    n = team
                    n3 = if(n3 != 0) -3 else n;
                    if(n3 == 0 || autoTeamMode) {
                        n3 = n2 % 2;
                        val p = PlayerInternal.i(spawnPoint)
                        if (p != null) {
                            n3 = p.s
                        } else if (room.teamMode != null) {
                            n3 = room.teamMode!!.autoTeamAssign(room, spawnPoint, this)
                        }
                        bl = true;
                    } else if(n3 != -1) {
                        --n3;
                        bl = false;
                    } else {
                        bl = false;
                    }
                    if(n3 == -1 || self.s == n3) return@block29
                    if(!t.bU.D) return@block30
                    self.s = n3;
                    n = 0;
                    return@block31
                }
                n = n4;
                if(t.bU.I) return@block31
                n = n4;
                if(t.bU.A == self) return@block31
                //t.a("row.setOnClickListener", "Clicked but not server or proxy controller");
            }
            n = 0;
        }
        n4 = n;
        if(self.l != n2) {
            n4 = n;
            if(n2 != -1) {
                if(t.bU.D) {
                    t.bU.a(self, n2);
                    n4 = n;
                } else if(t.bU.I || t.bU.A == self) {
                    n = if(bl) -1 else n3;
                    val ae2 = t.bU;
                    val p2 = self;
                    var n5: Integer? = Integer(n);
                    var obj = "";
                    if(n5 != null) {
                        obj = " " + n5;
                    }
                    if(!ae2.I && ae2.A == p2) {
                        ae2.i("-self_move " + (n2 + 1) + obj);
                        n4 = 0;
                    } else {
                        ae2.i("-move " + (p2.l + 1) + " " + (n2 + 1) + obj);
                        n4 = 0;
                    }
                } else {
                    //k.a("row.setOnClickListener", "Clicked but not server or proxy controller");
                    n4 = n;
                }
            }
        }
        if(n4 != 0) {
            if(bl) {
                t.bU.b(self, -1);
            } else {
                t.bU.b(self, n3);
            }
        }
        t.bU.b();
        t.bU.p();
    }
}