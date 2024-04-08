package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import me.gabryon.kodeedelivery.utility.debugContext

@RegisterClass
class LevelManager : Node() {

    @Export
    @RegisterProperty
    var currentLevel: Int = 0

    @Export
    @RegisterProperty
    var maximumLevel: Int = 0

    @Export
    @RegisterProperty
    lateinit var scoreManager: ScoreManager

    @RegisterFunction
    override fun _ready() {
        scoreManager.nextLevel.connect(this, LevelManager::onNextLevel)
    }

    @RegisterFunction
    fun onNextLevel() {
        currentLevel = when (currentLevel) {
            maximumLevel -> maximumLevel
            else -> currentLevel + 1
        }

        debugContext {
            info<LevelManager>("Current Level = $currentLevel")
        }
    }

}