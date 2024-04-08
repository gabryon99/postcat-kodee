package me.gabryon.kodeedelivery.utility

import godot.global.GD

const val DEBUG = true
object DebugContext {
    inline fun <reified T> info(msg: String) {
        GD.prints("[info][${T::class}] :: $msg")
    }
}

inline fun debugContext(body: DebugContext.() -> Unit) {
    if (DEBUG) body(DebugContext)
}
