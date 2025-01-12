/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.scripts

import io.github.rwpp.AppContext
import io.github.rwpp.appKoin
import io.github.rwpp.commands
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.event.Event
import io.github.rwpp.event.EventPriority
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.game.Game
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.net.Client
import io.github.rwpp.net.InternalPacketType
import io.github.rwpp.net.Net
import io.github.rwpp.net.Packet
import io.github.rwpp.platform.Platform
import io.github.rwpp.projectVersion
import party.iroiro.luajava.ClassPathLoader.BufferOutputStream
import party.iroiro.luajava.lua54.Lua54
import party.iroiro.luajava.value.RefLuaValue
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate")
object Scripts {
    val lua = Lua54()

    init {
        try {
            lua.setExternalLoader { module, _ ->
                val extension = appKoin.get<ExternalHandler>()
                    .getAllExtensions()
                    .getOrThrow()
                    .firstOrNull { it.config.id == module.split("/").first() }
                    ?: throw IllegalArgumentException("Extension not found: $module")


                val entry = extension.zipFile.getEntry(module.removePrefix(extension.config.id + "scripts/"))
                extension.zipFile.getInputStream(entry).use { input ->
                    val output = ByteArrayOutputStream()
                    val bytes = ByteArray(4096)
                    var i: Int
                    do {
                        i = input.read(bytes)
                        if (i != -1) {
                            output.write(bytes, 0, i)
                        }
                    } while (i != -1)
                    val buffer = ByteBuffer.allocateDirect(output.size())
                    output.writeTo(BufferOutputStream(buffer))
                    buffer.flip()
                    buffer
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        lua["scripts"] = lua
        lua["game"] = appKoin.get<Game>()
        lua["net"] = appKoin.get<Net>()
        lua["modManager"] = appKoin.get<ModManager>()
        lua["config"] = appKoin.get<ConfigIO>()
        lua["commands"] = commands
        lua["isAndroid"] = Platform.isAndroid()
        lua["isDesktop"] = Platform.isDesktop()
        lua["version"] = projectVersion

        @Suppress("UNCHECKED_CAST")
        lua.register("filterEvents") { _, args ->
            GlobalEventChannel.filter(
                Class.forName(
                    "io.github.rwpp.event.events." + args[0].toJavaObject() as String
                ).kotlin as KClass<Event>
            )
                .subscribeAlways(
                    priority = if (args.size > 2)
                        EventPriority.valueOf(args[1].toJavaObject() as String)
                    else EventPriority.NORMAL
                ) {
                    (args[if (args.size > 2) 2 else 1] as RefLuaValue).call(it)
                }

            arrayOf()
        }

        @Suppress("UNCHECKED_CAST")
        lua.register("filterEventsOnce") { _, args ->
            GlobalEventChannel.filter(
                Class.forName(
                    "io.github.rwpp.event.events." + args[0].toJavaObject() as String
                ).kotlin as KClass<Event>
            )
                .subscribeOnce(
                    priority = if (args.size > 2)
                        EventPriority.valueOf(args[1].toJavaObject() as String)
                    else EventPriority.NORMAL
                ) {
                    args[if (args.size > 2) 2 else 1].call(it)
                }

            arrayOf()
        }

        lua.register("registerCommand") { _, args ->
            commands.register<Any?>(
                args[0].toJavaObject() as String,
                args[1].toJavaObject() as String,
                args[2].toJavaObject() as String?,
            ) { a, p -> if (p != null) args[3].call(a, p) }

            arrayOf()
        }

        lua.register("onExit") { _, args ->
            appKoin.get<AppContext>().onExit {
                args[0].call()
            }

            arrayOf()
        }

        @Suppress("UNCHECKED_CAST")
        lua.register("registerPacketListener") { _, args ->
            val packetType = InternalPacketType.valueOf(args[0].toJavaObject() as String)
            appKoin.get<Net>().listeners[packetType] = args[1].toProxy(Function2::class.java) as (Client, Packet) -> Unit
            arrayOf()
        }
    }

    fun loadScript(id: String, src: String) {
        try {
            val body = """
                local extension = '$id' 
                local info = function(msg) 
                    local logger = java.method(java.import('io.github.rwpp.GlobalKt'), 'getLogger')() 
                    logger:info('[' .. extension .. '] ' .. msg) 
                end
                local getConfig = function(key) 
                    return config:readSingleConfig(extension, key)
                end
                local setConfig = function(key, value) 
                    config:saveSingleConfig(extension, key, value)
                end
                local broadcast = function(eventClass, ...) 
                    java.import('io.github.rwpp.event.EventKt'):broadcastIn(java.import('io.github.rwpp.event.events.' .. eventClass)(...))
                end
                local reply = function(player, message, title, color)
                     local gameRoom = game:getGameRoom()
                     gameRoom:sendMessageToPlayer(player, title or extension, message, color or -1)
                end
                local functionN = function(count, func) 
                    return java.proxy('kotlin.jvm.functions.Function' .. count, func)
                end
            """.trimIndent().replace("\n", " ")
            lua.run("$body $src")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}