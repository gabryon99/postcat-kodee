package me.gabryon.kodeedelivery.managers

import godot.AudioStreamPlayer3D
import godot.Node
import godot.Node3D
import godot.PackedScene
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import me.gabryon.kodeedelivery.actors.Dog
import me.gabryon.kodeedelivery.actors.Kodee
import me.gabryon.kodeedelivery.levels.*
import me.gabryon.kodeedelivery.utility.child

@RegisterClass
class LevelManager : Node() {

    data class NextLevel(val newLevel: Int, val newLevelLogic: LevelLogic)

    //region Level Management
    private val levels = arrayOf(
        DebugLevel0,
        Level1,
        Level2,
        Level3
    )

    @Export
    @RegisterProperty
    lateinit var scoreManager: ScoreManager

    @Export
    @RegisterProperty
    lateinit var bottomMailbox: PackedScene

    @Export
    @RegisterProperty
    lateinit var topMailbox: PackedScene

    @Export
    @RegisterProperty
    var sideOffset: Double = 0.5

    var currentLevel: Int = -1
        private set

    private lateinit var currentLevelLogic: LevelLogic
    private lateinit var mailboxGenerator: MailboxGenerator
    //endregion

    //region Actors
    @Export
    @RegisterProperty
    lateinit var world: Node3D
    @Export
    @RegisterProperty
    lateinit var kodee: Kodee
    @Export
    @RegisterProperty
    lateinit var dog: Dog
    //endregion

    private val nextLevelSound by child<AudioStreamPlayer3D>("NextLevelSound")

    @RegisterFunction
    override fun _ready() {
        // Setup managers first and then create the mailbox generator
        scoreManager.nextLevel.connect(this, LevelManager::onNextLevel)
        // Level -1 does not exist, change immediately to the first actual level
        levels.nextLevel(currentLevel).run {
            currentLevel = newLevel
            currentLevelLogic = newLevelLogic
            kodee.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            dog.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            scoreManager.pointsToNextLevel = newLevelLogic.pointsToNextLevel
        }

        mailboxGenerator = MailboxGenerator(
            currentLevelLogic, world, kodee, sideOffset, bottomMailbox, topMailbox, scoreManager
        )
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        mailboxGenerator.runLevelLogic(delta)
    }

    @RegisterFunction
    fun onNextLevel() {
        levels.nextLevel(currentLevel).run {
            currentLevel = newLevel

            if (newLevelLogic != currentLevelLogic) {
                nextLevelSound.play()
            }

            currentLevelLogic = newLevelLogic
            scoreManager.pointsToNextLevel = newLevelLogic.pointsToNextLevel
            kodee.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            dog.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            mailboxGenerator.changeLevelLogic(newLevelLogic)
        }
    }

    /**
     * Generates the next level based on the current level.
     *
     * @param currentLevel The current level.
     * @return The next level.
     */
    private fun Array<LevelLogic>.nextLevel(currentLevel: Int): NextLevel {
        val newLevel = (currentLevel + 1).coerceAtMost(lastIndex)
        val newLogic = this[newLevel]
        return NextLevel(newLevel, newLogic)
    }
}