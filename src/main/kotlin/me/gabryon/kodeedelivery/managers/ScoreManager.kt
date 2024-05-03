package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.annotation.*
import godot.signals.signal
import me.gabryon.kodeedelivery.utility.safeAdd

@RegisterClass
class ScoreManager : Node() {

    @RegisterSignal
    val nextLevel by signal()

    @RegisterSignal
    val scoreChanged by signal<Int>("newScore")

    @Export
    @RegisterProperty
    lateinit var levelManager: LevelManager

    @Export
    @RegisterProperty
    lateinit var comboManager: ComboManager

    private var storedScore = 0

    @RegisterFunction
    fun increaseScore(points: Int) {
        require(points >= 0) { "The points to add must be positive." }

        val multipliedPoints = (points * comboManager.currentComboMultiplier).toInt()
        val newScore = ScoreStorage.userScore safeAdd multipliedPoints

        storedScore = storedScore safeAdd points
        ScoreStorage.userScore = newScore

        scoreChanged.emit(newScore)

        if (levelManager.pointsToNextLevel != -1 && newScore >= levelManager.pointsToNextLevel) {
            storedScore = 0
            nextLevel.emit()
        }
    }
}
