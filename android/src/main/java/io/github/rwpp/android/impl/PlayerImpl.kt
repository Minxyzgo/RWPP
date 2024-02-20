/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import com.corrodinggames.rts.game.p
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.data.PlayerData
import io.github.rwpp.net.Client

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
        get() = player.w ?: ""
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
        get() = player.B ?: -1
    override val color: Int
        get() = player.D ?: -1
    override val isSpectator: Boolean
        get() = team == -3
    override val isAI: Boolean
        get() = player.x
    override val difficulty: Difficulty?
        get() = if(isAI) player.y.let { Difficulty.entries[it + 2] } else null
    override val data: PlayerData = PlayerData()
    override val client: Client by lazy {
        ClientImpl(GameEngine.t().bU.c(player))
    }


    override fun applyConfigChange(
        spawnPoint: Int,
        team: Int,
        color: Int?,
        startingUnits: Int?,
        aiDifficulty: Difficulty?,
        changeTeamFromSpawn: Boolean
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
                    n3 = (aiDifficulty?.ordinal?.minus(2)) ?: -99
                    //k.d("newAiDifficultyValue:".concat(String.valueOf(n3)));
                    if(t.bU.D) {
                        player.A = if(n3 == -99) null else Integer.valueOf(n3);
                    }
                    n3 = startingUnits ?: -99
                    //k.d("startingUnits:" + this.c);
                    if(t.bU.D) {
                        player.B = if(n3 == -99) null else Integer.valueOf(n3);
                    }
                    n3 = color ?: -99
                    //k.d("newPlayerColorValue:".concat(String.valueOf(n3)));
                    if(t.bU.D) {
                        player.D = if(n3 == -99) null else Integer.valueOf(n3);
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
                    if(n3 == 0) {
                        n3 = n2 % 2;
                        bl = true;
                    } else if(n3 != -1) {
                        --n3;
                        bl = false;
                    } else {
                        bl = false;
                    }
                    if(n3 == -1 || player.s == n3) return@block29
                    if(!t.bU.D) return@block30
                    player.s = n3;
                    n = 0;
                    return@block31
                }
                n = n4;
                if(t.bU.I) return@block31
                n = n4;
                if(t.bU.A == player) return@block31
                //t.a("row.setOnClickListener", "Clicked but not server or proxy controller");
            }
            n = 0;
        }
        n4 = n;
        if(player.l != n2) {
            n4 = n;
            if(n2 != -1) {
                if(t.bU.D) {
                    t.bU.a(player, n2);
                    n4 = n;
                } else if(t.bU.I || t.bU.A == player) {
                    n = if(bl) -1 else n3;
                    val ae2 = t.bU;
                    val p2 = player;
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
                t.bU.b(player, -1);
            } else {
                t.bU.b(player, n3);
            }
        }
        t.bU.b();
        t.bU.p();
    }
}