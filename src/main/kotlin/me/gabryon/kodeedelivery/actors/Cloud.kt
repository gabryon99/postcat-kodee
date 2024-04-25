package me.gabryon.kodeedelivery.actors

import godot.DisplayServer
import godot.Sprite2D
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty

@RegisterClass
class Cloud : Sprite2D() {

    @Export
    @RegisterProperty
    var speed = 1.0f

    @RegisterFunction
    override fun _process(delta: Double) {
        val displaySize = DisplayServer.screenGetSize()
        val size = texture!!.getSize()
        positionMutate {
            x += speed * delta
            when {
                speed < 0 && x <= -size.x -> {
                    x = displaySize.x + size.x
                }
                speed > 0 && x >= displaySize.x + size.x -> {
                    x = -size.x
                }
            }
        }
    }
}