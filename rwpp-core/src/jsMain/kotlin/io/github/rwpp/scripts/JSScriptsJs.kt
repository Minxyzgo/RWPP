package io.github.rwpp.scripts

import io.github.rwpp.core.Initialization

actual object JSScripts : Initialization {
    override fun init() {
        // JS 端可直接用全局对象
        // window.game = ...
        // window.externalHandler = ...
        // ...
    }

    override fun loadScript(id: String, src: String) {
        // 直接用 js("eval(...)"), 注意安全性
        js("eval(src)")
    }
} 