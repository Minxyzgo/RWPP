/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.github.rwpp.command

import io.github.rwpp.command.CommandHandler.ResponseType.*


typealias CommandRunner<T> = (args: Array<String>, params: T) -> Unit

/**
 * Parses command syntax.
 *
 * Adapted from [arc](https://github.com/Anuken/Arc/blob/master/arc-core/src/arc/util/CommandHandler.java)
 */
class CommandHandler(prefix: String) {
    var prefix: String = ""

    private val commands: MutableMap<String, Command> = mutableMapOf()
    private val orderedCommands: MutableList<Command> = mutableListOf()

    /** Creates a command handler with a specific command prefix. */
    init {
        this.prefix = prefix
    }

    /** Handles a message with no additional parameters. */
    fun handleMessage(message: String?): CommandResponse {
        return handleMessage(message, null)
    }

    /** Handles a message with optional extra parameters. Runs the command if successful.
     * @return a response detailing whether the command was handled, and what went wrong, if applicable.
     */
    @Suppress("UNCHECKED_CAST")
    fun handleMessage(message: String?, params: Any?): CommandResponse {
        @Suppress("NAME_SHADOWING")
        var message: String? = message
        if (message == null || (!message.startsWith(prefix))) return CommandResponse(NoCommand, null, null)

        message = message.substring(prefix.length)

        val commandStr: String = if (message.contains(" ")) message.substring(0, message.indexOf(" ")) else message
        var argStr: String = if (message.contains(" ")) message.substring(commandStr.length + 1) else ""

        val result: MutableList<String> = mutableListOf()

        val command: Command? = commands[commandStr.lowercase()]

        if (command != null) {
            var index = 0
            var satisfied = false

            while (true) {
                if (index >= command.params.size && argStr.isNotEmpty()) {
                    return CommandResponse(ManyArguments, command, commandStr)
                } else if (argStr.isEmpty()) break

                if (command.params[index]!!.optional || index >= command.params.size - 1 || command.params[index + 1]!!.optional) {
                    satisfied = true
                }

                if (command.params[index]!!.variadic) {
                    result.add(argStr)
                    break
                }

                val next: Int = argStr.indexOf(" ")
                if (next == -1) {
                    if (!satisfied) {
                        return CommandResponse(FewArguments, command, commandStr)
                    }
                    result.add(argStr)
                    break
                } else {
                    val arg: String = argStr.substring(0, next)
                    argStr = argStr.substring(arg.length + 1)
                    result.add(arg)
                }

                index++
            }

            if (!satisfied && command.params.isNotEmpty() && !command.params[0]!!.optional) {
                return CommandResponse(FewArguments, command, commandStr)
            }

            (command.runner as CommandRunner<Any?>)(result.toTypedArray(), params)

            return CommandResponse(Valid, command, commandStr)
        } else {
            return CommandResponse(UnknownCommand, null, commandStr)
        }
    }

    fun handleCommandMessage(line: String, params: Any?, reply: (String) -> Unit) {
        val response: CommandResponse = handleMessage(line, params)

        when (response.type) {
            UnknownCommand -> {
                reply("Invalid command. Type '/help' for help. (输入的命令无效, 请使用'/help'获取帮助.)")
            }
            FewArguments -> {
                reply("Too few command arguments (输入的参数太少). Usage: /" + response.command!!.text + " " + response.command!!.paramText)
            }
            ManyArguments -> {
                reply("Too many command arguments (输入的参数太多). Usage: /" + response.command!!.text + " " + response.command!!.paramText)
            }

            else -> {}
        }
    }

    fun removeCommand(text: String?) {
        val c: Command = commands[text] ?: return
        commands.remove(text)
        orderedCommands.remove(c)
    }

    /** Register a command which handles a zero-sized list of arguments and one parameter. */
    fun <T> register(text: String, description: String?, runner: CommandRunner<T>): Command {
        return register(text, "", description, runner)
    }

    /** Register a command which handles a list of arguments and one handler-specific parameter. <br></br>
     * argeter syntax is as follows: <br></br>
     * &lt;mandatory-arg-1&gt; &lt;mandatory-arg-2&gt; ... &lt;mandatory-arg-n&gt; [optional-arg-1] [optional-arg-2] <br></br>
     * Angle brackets indicate mandatory arguments. Square brackets to indicate optional arguments. <br></br>
     * All mandatory arguments must come before optional arguments. Arg names must not have spaces in them. <br></br>
     * You may also use the ... syntax after the arg name to designate that everything after it will not be split into extra arguments.
     * There may only be one such argument, and it must be at the end. For example, the syntax
     * &lt;arg1&gt arg2 will require a first argument, and then take any text after that and put it in the second argument, optionally. */
    fun <T> register(
        text: String,
        params: String,
        description: String?,
        runner: CommandRunner<T>
    ): Command {
        //remove previously registered commands
        orderedCommands.removeAll { c -> c.text == text }

        val cmd = Command(text, params, description, runner)
        commands[text.lowercase()] = cmd
        orderedCommands.add(cmd)
        return cmd
    }

    val commandList: MutableList<Command>
        get() = orderedCommands

    enum class ResponseType {
        NoCommand, UnknownCommand, FewArguments, ManyArguments, Valid
    }

    class Command(val text: String, val paramText: String, val description: String?, val runner: CommandRunner<*>) {
        val params: Array<CommandParam?>

        init {
            val pSplit: List<String> = paramText.split(" ")
            if (paramText.isBlank()) {
                params = arrayOfNulls(0)
            } else {
                params = arrayOfNulls(pSplit.size)

                var hadOptional = false

                for (i in params.indices) {
                    val param: String = pSplit[i]

                    require(param.length > 2) { "Malformed param '$param'" }

                    val l: Char = param[0]
                    val r: Char = param[param.length - 1]
                    val optional: Boolean
                    var variadic = false

                    if (l == '<' && r == '>') {
                        require(!hadOptional) { "Can't have non-optional param after optional param!" }
                        optional = false
                    } else if (l == '[' && r == ']') {
                        optional = true
                    } else {
                        throw IllegalArgumentException("Malformed param '$param'")
                    }

                    if (optional) hadOptional = true

                    var fname: String = param.substring(1, param.length - 1)
                    if (fname.endsWith("...")) {

                        require(i == params.size - 1) { "A variadic parameter should be the last parameter!" }

                        fname = fname.substring(0, fname.length - 3)
                        variadic = true
                    }

                    params[i] = CommandParam(fname, optional, variadic)
                }
            }
        }
    }

    class CommandParam(val name: String, val optional: Boolean, val variadic: Boolean)

    class CommandResponse(val type: ResponseType, val command: Command?, val runCommand: String?)
}