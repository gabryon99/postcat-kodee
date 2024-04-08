package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import me.gabryon.kodeedelivery.MailboxGenerator
import me.gabryon.kodeedelivery.levels.DebugLevel
import me.gabryon.kodeedelivery.utility.debugContext

@RegisterClass
class LevelManager : Node() {

    val levels = arrayOf(
        0,
        2500,
        5000,
        10000,
        15000,
        20000,
        30000,
        45000,
        60000,
        80000,
        100000
    )

    @Export
    @RegisterProperty
    var currentLevel: Int = 0

    @Export
    @RegisterProperty
    lateinit var scoreManager: ScoreManager

    @Export
    @RegisterProperty
    lateinit var mailboxGenerator: MailboxGenerator

    var currentLevelLogic = DebugLevel()

    @RegisterFunction
    override fun _ready() {
        scoreManager.nextLevel.connect(this, LevelManager::onNextLevel)
    }

    @RegisterFunction
    fun onNextLevel() {
        currentLevel = when (currentLevel) {
            levels.size -> levels.size
            else -> currentLevel + 1
        }

        debugContext {
            info<LevelManager>("Current Level = $currentLevel")
        }
    }


    @RegisterFunction
    override fun _process(delta: Double) {
        mailboxGenerator.spawnMailboxes(currentLevelLogic)
    }

}