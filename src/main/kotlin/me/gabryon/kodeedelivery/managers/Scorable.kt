package me.gabryon.kodeedelivery.managers

import godot.annotation.Export
import godot.annotation.RegisterProperty
import godot.signals.Signal1

/**
 * Represents an object that can be scored. It is used to track the score of an object.
 */
interface Scorable {
    val scored: Signal1<Int>

    @Export
    @RegisterProperty
    var score: Int
}