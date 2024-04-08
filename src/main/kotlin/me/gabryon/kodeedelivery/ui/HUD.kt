package me.gabryon.kodeedelivery.ui

import godot.Control
import godot.Label
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import me.gabryon.kodeedelivery.managers.ScoreManager
import me.gabryon.kodeedelivery.utility.debugContext

@RegisterClass
class HUD : Control() {

    @Export
    @RegisterProperty
    lateinit var scoreManager: ScoreManager

    @Export
    @RegisterProperty
    lateinit var scoreLabel: Label

    @RegisterFunction
    override fun _ready() {
        scoreManager.scoreChanged.connect(this, HUD::onScoreChanged)
    }

    @RegisterFunction
    fun onScoreChanged(oldScore: Int, newScore: Int) {
        debugContext {
            info<HUD>("Old Score: $oldScore, New Score: $newScore")
        }
        scoreLabel.text = "Score: $newScore"
    }

}