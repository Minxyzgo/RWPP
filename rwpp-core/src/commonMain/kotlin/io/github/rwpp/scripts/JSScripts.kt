package io.github.rwpp.scripts

import io.github.rwpp.core.Initialization

expect object JSScripts : Initialization {
    fun loadScript(id: String, src: String)
} 