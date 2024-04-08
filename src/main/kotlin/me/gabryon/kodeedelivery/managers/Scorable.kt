package me.gabryon.kodeedelivery.managers

import godot.annotation.Export
import godot.annotation.RegisterProperty
import godot.signals.Signal1

interface Scorable {
    val scored: Signal1<Int>

    @Export
    @RegisterProperty
    var score: Int
}