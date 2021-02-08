package ru.kontur.kinfra.logging.impl

import ru.kontur.kinfra.logging.LoggingContext.Element

internal class ContextElementSet : AbstractSet<Element> {

    private val array: Array<Element>

    val asMap: Map<String, String> = MapView()

    constructor(element: Element) {
        array = arrayOf(element)
    }

    constructor(parent: ContextElementSet, element: Element) {
        val parentArray = parent.array
        val array = parentArray.copyOf(parentArray.size + 1)
        array[array.size - 1] = element

        @Suppress("UNCHECKED_CAST")
        this.array = array as Array<Element>
    }

    override val size: Int
        get() = array.size

    override fun contains(element: Element) = array.contains(element)

    override fun iterator() = array.iterator()

    override fun toString() = array.contentToString()

    inner class MapView : AbstractMap<String, String>() {

        override val size: Int
            get() = array.size

        override val entries: Set<Map.Entry<String, String>>
            get() = this@ContextElementSet

        override fun containsKey(key: String) = array.any { it.key == key }

        override fun containsValue(value: String) = array.any { it.value == value }

        override fun get(key: String) = array.find { it.key == key }?.value

    }

}
