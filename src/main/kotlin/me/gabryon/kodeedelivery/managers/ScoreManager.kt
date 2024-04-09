package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.annotation.RegisterSignal
import godot.signals.signal
import me.gabryon.kodeedelivery.levels.LevelLogic
import me.gabryon.kodeedelivery.utility.debugContext

@RegisterClass
class ScoreManager : Node() {

    @RegisterSignal
    val nextLevel by signal()

    @RegisterSignal
    val scoreChanged by signal<Int, Int>("oldScore", "newScore")

    @Export
    @RegisterProperty
    var currentScore: Int = 0

    @Export
    @RegisterProperty
    lateinit var levelManager: LevelManager

    var pointsToNextLevel: Int = Int.MAX_VALUE
    private var storedScore = 0

    @Export
    @RegisterProperty
    lateinit var comboManager: ComboManager

    @RegisterFunction
    fun increaseScore(points: Int) {
        require(points >= 0) { "The points to add must be positive." }

        val oldScore = currentScore
        currentScore += (points * comboManager.currentComboMultiplier).toInt()

        storedScore += points

        debugContext {
            scoreChanged.emit(oldScore, currentScore)
            info<ScoreManager>("NextLevel = ${pointsToNextLevel}, Stored Score = ${storedScore}, Current Score = $currentScore, New Points = $points, Multiplier = ${comboManager.currentComboMultiplier}")
        }

        if (storedScore >= pointsToNextLevel) {
            storedScore = 0
            nextLevel.emit()
        }
    }
}
