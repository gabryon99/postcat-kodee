package me.gabryon.kodeedelivery.managers

import ch.hippmann.godot.utilities.logging.debug
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
    private val levels: Array<LevelLogic> = arrayOf(
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
    lateinit var smallMailBoxScene: PackedScene

    @Export
    @RegisterProperty
    lateinit var tallMailBoxScene: PackedScene

    @Export
    @RegisterProperty
    var sideOffset: Double = 0.5

    private var currentLevel: Int = -1

    private lateinit var currentLevelLogic: LevelLogic
    private lateinit var mailboxGenerator: MailBoxGenerator
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

    val pointsToNextLevel: Int
        get() = currentLevelLogic.pointsToNextLevel


    private val nextLevelSound by child<AudioStreamPlayer3D>("NextLevelSound")

    @RegisterFunction
    override fun _ready() {
        // Setup managers first and then create the mailbox generator
        scoreManager.nextLevel.connect(this, LevelManager::onNextLevel)
        // Level -1 does not exist, change immediately to the first actual level
        levels.nextLevel(currentLevel).run {
            currentLevel = newLevel
            currentLevelLogic = newLevelLogic
            debug("[setting first level] :: level logic = $newLevelLogic, pointsToNextLevel=$pointsToNextLevel")

            debug("[setting first level] :: maximum actor speeds set")
            kodee.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            kodee.initialAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            kodee.angularSpeed = currentLevelLogic.maximumCharacterSpeed

            dog.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            dog.initialAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            dog.angularSpeed = currentLevelLogic.maximumCharacterSpeed
        }

        mailboxGenerator = MailBoxGenerator(
            currentLevelLogic, world, kodee, sideOffset, smallMailBoxScene, tallMailBoxScene, scoreManager, dog
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
            debug("[changing level] :: level logic = $newLevelLogic, pointsToNextLevel=$pointsToNextLevel")

            debug("[changing level] :: maximum actor speeds set")
            kodee.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            kodee.initialAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            kodee.angularSpeed = currentLevelLogic.maximumCharacterSpeed

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