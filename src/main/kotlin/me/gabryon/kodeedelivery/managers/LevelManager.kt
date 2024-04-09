package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.annotation.Export
import godot.annotation.IntRange
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import me.gabryon.kodeedelivery.MailboxGenerator
import me.gabryon.kodeedelivery.actors.Dog
import me.gabryon.kodeedelivery.actors.Kodee
import me.gabryon.kodeedelivery.levels.DebugLevel0
import me.gabryon.kodeedelivery.levels.DebugLevel1
import me.gabryon.kodeedelivery.levels.Level1
import me.gabryon.kodeedelivery.levels.LevelLogic
import me.gabryon.kodeedelivery.utility.debugContext

@RegisterClass
class LevelManager : Node() {

    private val levels = arrayOf(
        DebugLevel0,
        DebugLevel1
    )

    @Export
    @RegisterProperty
    @IntRange(0, 1)
    var currentLevel: Int = 0

    @Export
    @RegisterProperty
    lateinit var scoreManager: ScoreManager

    @Export
    @RegisterProperty
    lateinit var mailboxGenerator: MailboxGenerator

    @Export
    @RegisterProperty
    lateinit var kodee: Kodee
    @Export
    @RegisterProperty
    lateinit var dog: Dog

    private lateinit var currentLevelLogic: LevelLogic

    @RegisterFunction
    override fun _ready() {
        scoreManager.nextLevel.connect(this, LevelManager::onNextLevel)
        goNextLevel()
    }

    @RegisterFunction
    fun onNextLevel() {
        currentLevel = when (currentLevel) {
            levels.size - 1 -> levels.size - 1
            else -> currentLevel + 1
        }
        goNextLevel()
    }

    private fun goNextLevel() {
        currentLevelLogic = levels[currentLevel]
        mailboxGenerator.setLevelLogic(currentLevelLogic)
        if (currentLevel + 1 != levels.size) {
            scoreManager.pointsToNextLevel = levels[currentLevel + 1].pointsToNextLevel
        }
        // kodee.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
        // dog.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        mailboxGenerator.runLevelLogic(delta)
    }
}