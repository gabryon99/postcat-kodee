package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.annotation.*
import godot.signals.signal
import me.gabryon.kodeedelivery.utility.debugContext
import me.gabryon.kodeedelivery.utility.safeAdd

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
        val newScore = (points * comboManager.currentComboMultiplier).toInt()

        currentScore = currentScore safeAdd newScore
        storedScore = storedScore safeAdd points

        scoreChanged.emit(oldScore, currentScore)

        debugContext {
//            info<ScoreManager>("NextLevel = ${pointsToNextLevel}, Stored Score = ${storedScore}, Current Score = $currentScore, New Points = $points, Multiplier = ${comboManager.currentComboMultiplier}")
        }

        if (storedScore >= pointsToNextLevel) {
            storedScore = 0
            nextLevel.emit()
        }
    }
}
