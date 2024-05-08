package me.gabryon.kodeedelivery.ui

import godot.Label
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import me.gabryon.kodeedelivery.managers.ScoreStorage

@RegisterClass
class PrizeLabel : Label() {
    @RegisterFunction
    override fun _ready() {
        text = when {
            ScoreStorage.userScore > ScoreStorage.MINIMUM_SCORE_FOR_PRIZE -> {
                "Come to the JetBrains booth to reclaim your special limited gadget!"
            }
            else -> {
                "Get to ${ScoreStorage.MINIMUM_SCORE_FOR_PRIZE} for a\nchance to win a prize!"
            }
        }
    }
}