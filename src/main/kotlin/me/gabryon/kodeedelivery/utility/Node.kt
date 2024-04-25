package me.gabryon.kodeedelivery.utility

import godot.Node
import godot.core.NodePath
import kotlin.reflect.KProperty

class Child<T>(
    private val parent: Node,
    private val path: NodePath,
    private val initialize: (T) -> Unit
) {
    private var loaded: T? = null

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        loaded ?: (parent.getNode(path)!! as T).apply {
            loaded = this
            initialize(this)
        }
}

fun <T : Node> Node.child(path: String, initialize: (T) -> Unit = {}): Child<T> =
    Child(this, NodePath(path), initialize)

inline fun <reified T : Node> Node.child(noinline initialize: (T) -> Unit = {}): Child<T> =
    child(T::class.simpleName!!, initialize)