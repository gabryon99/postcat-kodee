package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.annotation.RegisterSignal
import godot.signals.signal
import me.gabryon.kodeedelivery.utility.debugContext

@RegisterClass
class ScoreManager : Node() {

    @Export
    @RegisterSignal
    val nextLevel by signal()

    @Export
    @RegisterSignal
    val scoreChanged by signal<Int, Int>("oldScore", "newScore")

    @Export
    @RegisterProperty
    var currentScore: Int = 0

    @Export
    @RegisterProperty
    var pointsToNextLevel: Int = 1000

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

        if (storedScore >= pointsToNextLevel) {
            storedScore = 0
            nextLevel.emit()
        }

        debugContext {
            scoreChanged.emit(oldScore, currentScore)
            info<ScoreManager>("Emitted!!!")
            info<ScoreManager>("Current Score = $currentScore, New Points = $points, Multiplier = ${comboManager.currentComboMultiplier}")
        }
    }
}
