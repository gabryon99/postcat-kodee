package me.gabryon.kodeedelivery.managers

import godot.AudioStreamPlayer3D
import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import me.gabryon.kodeedelivery.utility.child

@RegisterClass
class MusicManager : Node() {

    private val bgMusic by child<AudioStreamPlayer3D>("BackgroundMusic")

    @RegisterFunction
    fun onDistanceChange(distance: Int) {

        when (distance) {
            Referee.VERY_CLOSE -> {
                bgMusic.pitchScale = 1.1f
            }

            Referee.CLOSE -> {
                bgMusic.pitchScale = 1.08f
            }

            Referee.FAR -> {
                bgMusic.pitchScale = 1.02f
            }

            else -> {
                bgMusic.pitchScale = 1.0f
            }
        }

    }
}