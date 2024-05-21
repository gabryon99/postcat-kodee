package me.gabryon.kodeedelivery.managers

import godot.*
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
    private var levels: Array<LevelLogic> = arrayOf(
        Level1(),
        Level2(),
        Level3(),
        Level4(),
        Level5(),
        Level6()
    )

    @Export
    @RegisterProperty
    lateinit var referee: Referee

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

    private var currentLevel = -1
    private var loops = 1

    private lateinit var currentLevelLogic: LevelLogic
    private lateinit var mailboxGenerator: MailBoxManager
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

    @Export
    @RegisterProperty
    lateinit var topLeftWarning: TextureRect

    @Export
    @RegisterProperty
    lateinit var bottomLeftWarning: TextureRect

    @Export
    @RegisterProperty
    lateinit var topRightWarning: TextureRect

    @Export
    @RegisterProperty
    lateinit var bottomRightWarning: TextureRect

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

            kodee.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            kodee.initialAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            kodee.angularSpeed = currentLevelLogic.maximumCharacterSpeed
            dog.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed * 1.13
            dog.initialAngularSpeed = currentLevelLogic.maximumCharacterSpeed * 1.13
            dog.angularSpeed = currentLevelLogic.maximumCharacterSpeed * 1.13

            referee.minMaxSpeedDog = dog.maximumAngularSpeed * 1.13
        }

        mailboxGenerator = MailBoxManager(
            currentLevelLogic,
            world,
            kodee,
            sideOffset,
            smallMailBoxScene,
            tallMailBoxScene,
            scoreManager,
            arrayOf(topLeftWarning, bottomLeftWarning, topRightWarning, bottomRightWarning)
        )
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        mailboxGenerator.runLevelLogic(delta)
    }

    @RegisterFunction
    fun onNextLevel() {

        if (currentLevel == levels.size) {
            currentLevel = -1
            loops += 1

            // Re-initialize the array of levels
            levels = arrayOf(
                Level1(MAXIMUM_CHARACTER_SPEED, Level1.DEFAULT_POINTS_TO_NEXT_LEVEL * loops),
                Level2(MAXIMUM_CHARACTER_SPEED, Level2.DEFAULT_POINTS_TO_NEXT_LEVEL * loops),
                Level3(MAXIMUM_CHARACTER_SPEED, Level3.DEFAULT_POINTS_TO_NEXT_LEVEL * loops),
                Level4(MAXIMUM_CHARACTER_SPEED, Level4.DEFAULT_POINTS_TO_NEXT_LEVEL * loops),
                Level5(MAXIMUM_CHARACTER_SPEED, Level5.DEFAULT_POINTS_TO_NEXT_LEVEL * loops),
                Level6(MAXIMUM_CHARACTER_SPEED, Level6.DEFAULT_POINTS_TO_NEXT_LEVEL * loops)
            )
        }

        levels.nextLevel(currentLevel).run {
            currentLevel = newLevel

            if (newLevelLogic != currentLevelLogic) {
                nextLevelSound.play()
            }

            currentLevelLogic = newLevelLogic

            kodee.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            kodee.angularSpeed = currentLevelLogic.maximumCharacterSpeed

            dog.maximumAngularSpeed = currentLevelLogic.maximumCharacterSpeed
            dog.angularSpeed = currentLevelLogic.maximumCharacterSpeed

            referee.minMaxSpeedDog = dog.maximumAngularSpeed * 0.9

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