package me.gabryon.kodeedelivery.ui

import godot.Texture2D
import godot.TextureRect
import godot.Tween
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterProperty
import godot.core.asNodePath
import godot.core.asStringName

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

    fun updateBar(value: Double) {
        texture = when (value) {
            in 0.0..<25.0 -> indicator3
            in 25.0..<50.0 -> indicator2
            in 50.0..<75.0 -> indicator1
            else -> indicator0
        }
    }
}