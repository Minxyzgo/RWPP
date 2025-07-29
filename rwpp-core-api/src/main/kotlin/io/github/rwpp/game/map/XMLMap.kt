/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.map

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@Suppress("unused", "MemberVisibilityCanBePrivate")
class XMLMap(val map: GameMap) {
    val root: Element
    val objectGroup: Element
    val width: Float
    val height: Float
    val tileWidth: Float
    val tileHeight: Float
    val objects = mutableListOf<MapObject>()
    val document: Document = factory.newDocumentBuilder().parse(map.openInputStream())

    init {
        root = document.firstChild as Element
        width = root.getAttribute("width").toFloat()
        height = root.getAttribute("height").toFloat()
        tileWidth = root.getAttribute("tilewidth").toFloat()
        tileHeight = root.getAttribute("tileheight").toFloat()

        objectGroup = root.getElementsByTagName("objectgroup").item(0) as Element

        val objects = objectGroup.getElementsByTagName("object")

        for (i in 0 until objects.length) {
            val obj = objects.item(i) as Element
            val id = obj.getAttribute("id")
            val x = obj.getAttribute("x").toFloat()
            val y = obj.getAttribute("y").toFloat()
            val width = obj.getAttribute("width").toFloat()
            val height = obj.getAttribute("height").toFloat()
            val type = obj.getAttribute("type")
            val properties = obj.getElementsByTagName("properties")

            this@XMLMap.objects.add(MapObject(id, x, y, width, height, type, properties))
        }
    }

    fun addMapObject(
        id: String,
        type: String,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        properties: (MutableList<Pair<String, String>>) -> Unit
    ) {
        val element = document.createElement("object")
        element.setAttribute("id", id)
        element.setAttribute("x", x.toString())
        element.setAttribute("y", y.toString())
        element.setAttribute("width", width.toString())
        element.setAttribute("height", height.toString())
        element.setAttribute("type", type)

        val propertiesList = mutableListOf<Pair<String, String>>()
        properties(propertiesList)

        val properties2 = document.createElement("properties")

        for (property in propertiesList) {
            val prop = document.createElement("property")
            prop.setAttribute("name", property.first)
            prop.setAttribute("value", property.second)
            properties2.appendChild(prop)
        }

        element.appendChild(properties2)
        objectGroup.appendChild(element)
    }

    fun saveToFile(path: String): File {
        val file = File(path)
        if (!file.exists()) {
            file.createNewFile()
        }
        val writer = java.io.FileWriter(path)
        val transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "no")
        transformer.transform(javax.xml.transform.dom.DOMSource(document), javax.xml.transform.stream.StreamResult(writer))
        writer.close()
        return file
    }

    class MapObject(
        val id: String,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val type: String,
        val properties: NodeList
    ) {
        @Suppress("unused")
        fun parseProperties(): MutableList<Pair<String, String>> {
            val result = mutableListOf<Pair<String, String>>()
            for (i in 0 until properties.length) {
                val property = properties.item(i) as Element
                val name = property.getAttribute("name")
                val value = property.getAttribute("value")
                result.add(name to value)
            }
            return result
        }
    }

    companion object {
        private val factory = DocumentBuilderFactory.newInstance()
    }
}