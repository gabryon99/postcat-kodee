package me.gabryon.kodeedelivery.ui

import godot.Control
import godot.Label
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import me.gabryon.kodeedelivery.managers.LevelManager
import me.gabryon.kodeedelivery.managers.ScoreManager
import me.gabryon.kodeedelivery.utility.debugContext

@RegisterClass
class HUD : Control() {

    @Export
    @RegisterProperty
    lateinit var scoreManager: ScoreManager
    @Export
    @RegisterProperty
    lateinit var levelManager: LevelManager

    @Export
    @RegisterProperty
    lateinit var scoreLabel: Label
    @Export
    @RegisterProperty
    lateinit var levelLabel: Label

    @RegisterFunction
    override fun _ready() {
        scoreManager.scoreChanged.connect(this, HUD::onScoreChanged)
        scoreManager.nextLevel.connect(this, HUD::onNextLevel)
    }

    @RegisterFunction
    fun onScoreChanged(oldScore: Int, newScore: Int) {
        scoreLabel.text = "Score: $newScore"
    }

    @RegisterFunction
    fun onNextLevel() {
        levelLabel.text = "Level: ${levelManager.currentLevel}"
    }
}