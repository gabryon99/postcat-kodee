package me.gabryon.kodeedelivery.ui

import ch.hippmann.godot.utilities.logging.debug
import godot.Texture2D
import godot.TextureRect
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.StringName
import godot.core.asStringName
import me.gabryon.kodeedelivery.managers.Referee

@RegisterClass
class Indicator : TextureRect() {
    @Export
    @RegisterProperty
    lateinit var indicator0: Texture2D

    @Export
    @RegisterProperty
    lateinit var indicator1: Texture2D

    @Export
    @RegisterProperty
    lateinit var indicator2: Texture2D

    @Export
    @RegisterProperty
    lateinit var indicator3: Texture2D

    @RegisterFunction
    fun onDistanceWithDogChanged(distance: StringName) {
        texture = when (distance.toString()) {
            Referee.FAR_AWAY ->indicator0
            Referee.FAR -> indicator1
            Referee.CLOSE -> indicator2
            else -> indicator3
        }
    }
}