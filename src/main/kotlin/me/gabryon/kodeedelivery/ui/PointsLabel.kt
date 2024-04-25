package me.gabryon.kodeedelivery.ui

import godot.Label
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import me.gabryon.kodeedelivery.managers.ScoreStorage

@RegisterClass
class PointsLabel : Label() {
    @RegisterFunction
    override fun _ready() {
        text = "${ScoreStorage.userScore} points!\nWell done!"
    }
}