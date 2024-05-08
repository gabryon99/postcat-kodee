package me.gabryon.kodeedelivery.ui

import godot.AnimationPlayer
import godot.Node3D
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.asStringName
import me.gabryon.kodeedelivery.utility.child

@RegisterClass
class StartScreen : Node3D() {

    private val animationPlayer by child<AnimationPlayer>()

    @RegisterFunction
    override fun _ready() {
        animationPlayer.play("start_game".asStringName())
    }
}