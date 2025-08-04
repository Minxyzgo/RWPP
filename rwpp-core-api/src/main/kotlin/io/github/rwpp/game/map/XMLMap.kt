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
    private val _objects = mutableListOf<MapObject>()

    val root: Element
    val objectGroup: Element
    val width: Float
    val height: Float
    val tileWidth: Float
    val tileHeight: Float
    var hasMapInfo: Boolean = false
    val objects: List<MapObject>
        get() = _objects
    val document: Document = factory.newDocumentBuilder().parse(map.openInputStream())

    init {
        root = document.firstChild as Element
        width = root.getAttribute("width").toFloat()
        height = root.getAttribute("height").toFloat()
        tileWidth = root.getAttribute("tilewidth").toFloat()
        tileHeight = root.getAttribute("tileheight").toFloat()
        objectGroup = (root.getElementsByTagName("objectgroup").item(0) as Element?) ?: run {
            val element = document.createElement("objectgroup")
            element.setAttribute("name", "Triggers")
            root.appendChild(element)
            element
        }

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

            if (obj.getAttribute("name") == "map_info")
                hasMapInfo = true

            _objects.add(MapObject(obj))
        }
    }

    fun addMapObject(
        id: String,
        type: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        properties: (MutableList<Pair<String, String>>) -> Unit
    ): MapObject {
        return addMapObject(
            id,
            type,
            x,
            y,
            width,
            height,
            *buildList { properties(this) }.toTypedArray()
        )
    }

    fun addMapObject(
        id: String,
        type: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        vararg properties: Pair<String, String>
    ): MapObject {
        val element = document.createElement("object")
        element.setAttribute("id", id)
        element.setAttribute("x", x.toString())
        element.setAttribute("y", y.toString())
        element.setAttribute("width", width.toString())
        element.setAttribute("height", height.toString())
        element.setAttribute("type", type)

        val properties2 = document.createElement("properties")

        for (property in properties) {
            val prop = document.createElement("property")
            prop.setAttribute("name", property.first)
            prop.setAttribute("value", property.second)
            properties2.appendChild(prop)
        }

        val mapObject = MapObject(element)
        _objects.add(mapObject)
        element.appendChild(properties2)
        objectGroup.appendChild(element)
        return mapObject
    }

    fun addMapInfo(type: String) {
        if (!hasMapInfo) {
            val element = document.createElement("object")
            element.setAttribute("name", "map_info")
            element.setAttribute("x", 0.toString())
            element.setAttribute("y", 0.toString())
            element.setAttribute("width", tileWidth.toString())
            element.setAttribute("height", tileHeight.toString())
            val properties = document.createElement("properties")
            val typeProperty = document.createElement("property")
            typeProperty.setAttribute("name", "type")
            typeProperty.setAttribute("value", type)
            properties.appendChild(typeProperty)
            element.appendChild(properties)
            _objects.add(MapObject(element))
            objectGroup.appendChild(element)
            hasMapInfo = true
        }
    }

    fun removeMapObject(id: String) {
        val objects = objectGroup.getElementsByTagName("object")
        for (i in 0 until objects.length) {
            val obj = objects.item(i) as Element
            if (obj.getAttribute("id") == id) {
                objectGroup.removeChild(obj)
                _objects.removeIf { it.id == id }
                break
            }
        }
    }

    fun saveToFile(path: String): File {
        addMapInfo("skirmish")
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

    inner class MapObject(
        val element: Element
    ) {
        var id: String
            get() = element.getAttribute("id")
            set(value) { element.setAttribute("id", value) }
        var x: Float
            get() = element.getAttribute("x").toFloat()
            set(value) { element.setAttribute("x", value.toString()) }
        var y: Float
            get() = element.getAttribute("y").toFloat()
            set(value) { element.setAttribute("y", value.toString()) }
        var width: Float
            get() = element.getAttribute("width").toFloat()
            set(value) { element.setAttribute("width", value.toString()) }
        var height: Float
            get() = element.getAttribute("height").toFloat()
            set(value) { element.setAttribute("height", value.toString()) }
        var type: String
            get() = element.getAttribute("type")
            set(value) { element.setAttribute("type", value) }
        val properties: NodeList
            get() = element.getElementsByTagName("properties").item(0).childNodes

        fun appendProperty(name: String, value: String) {
            val prop = document.createElement("property")
            prop.setAttribute("name", name)
            prop.setAttribute("value", value)
            element.getElementsByTagName("properties").item(0).appendChild(prop)
        }

        fun removeProperty(name: String) {
            val properties = element.getElementsByTagName("properties").item(0)
            for (i in 0 until properties.childNodes.length) {
                val property = properties.childNodes.item(i) as Element
                if (property.getAttribute("name") == name) {
                    properties.removeChild(property)
                    break
                }
            }
        }

        fun getProperty(name: String): String? {
            val properties = element.getElementsByTagName("properties").item(0)
            for (i in 0 until properties.childNodes.length) {
                val property = properties.childNodes.item(i) as Element
                if (property.getAttribute("name") == name) {
                    return property.getAttribute("value")
                }
            }
            return null
        }

        fun setProperty(name: String, value: String) {
            val properties = element.getElementsByTagName("properties").item(0)
            for (i in 0 until properties.childNodes.length) {
                val property = properties.childNodes.item(i) as Element
                if (property.getAttribute("name") == name) {
                    property.setAttribute("value", value)
                    break
                }
            }
        }

        fun hasProperty(name: String): Boolean {
            val properties = element.getElementsByTagName("properties").item(0)
            for (i in 0 until properties.childNodes.length) {
                val property = properties.childNodes.item(i) as Element
                if (property.getAttribute("name") == name) {
                    return true
                }
            }
            return false
        }

        @Suppress("unused")
        fun parseProperties(): List<Pair<String, String>> {
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