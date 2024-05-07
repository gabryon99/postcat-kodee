package me.gabryon.kodeedelivery.ui

import godot.Label
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import me.gabryon.kodeedelivery.managers.ScoreStorage

/**
 * Represents a label that displays the player's personal record.
 * This label will display the maximum user score stored in the singleton [ScoreStorage].
 */
@RegisterClass
class PersonalRecordLabel : Label() {
    @RegisterFunction
    override fun _ready() {
        text = "Your Score: ${ScoreStorage.maximumUserScore}/${ScoreStorage.MINIMUM_SCORE_FOR_PRIZE}"
    }
}