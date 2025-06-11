package io.github.rwpp.scripts

import io.github.rwpp.core.Initialization
import javax.script.ScriptEngineManager

actual object JSScripts : Initialization {
    private val engine = ScriptEngineManager().getEngineByName("nashorn")

    override fun init() {
        // 注册全局对象（根据实际需要补充）
        // engine.put("game", ...)
        // engine.put("externalHandler", ...)
        // engine.put("ui", ...)
        // ...
        initJSUI()
    }

    override fun loadScript(id: String, src: String) {
        val header = """
            var id = '$id';
            // function info(msg) { ... }
            // function getConfig(key) { ... }
            // ...
        """.trimIndent()
        engine.eval("$header\n$src")
    }

    private fun initJSUI() {
        // 注册UI相关API（可参考 LuaWidget 设计）
        // engine.put("createText", ...)
        // engine.put("createButton", ...)
        // ...
    }
} 