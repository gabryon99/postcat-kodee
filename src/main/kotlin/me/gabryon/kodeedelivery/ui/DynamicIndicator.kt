package me.gabryon.kodeedelivery.ui

import godot.AnimationPlayer
import godot.Control
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.asStringName
import me.gabryon.kodeedelivery.managers.Referee
import me.gabryon.kodeedelivery.utility.child
import me.gabryon.kodeedelivery.utility.debugLine

@RegisterClass
class DynamicIndicator : Control() {

    private val animationPlayer by child<AnimationPlayer>()
    private var prevDistance = Referee.FAR_AWAY

    @Export
    @RegisterProperty
    var animSpeed = 2.0f

    @RegisterFunction
    fun onDistanceChanged(distance: Int) {

        debugLine {
            val isAnimationReverse by isOnReverse(prevDistance, distance)
            val nextAnimationName = distance.toAnimationName()

            if (nextAnimationName != null) {
                animationPlayer.play(
                    nextAnimationName.asStringName(),
                    customSpeed = if (isAnimationReverse) -animSpeed else animSpeed
                )
            }
        }

        prevDistance = distance

    }

    private fun isOnReverse(prevDistance: Int, newDistance: Int): Boolean = when {
        prevDistance == Referee.FAR_AWAY && newDistance == Referee.FAR -> false
        prevDistance == Referee.FAR && newDistance == Referee.CLOSE -> false
        prevDistance == Referee.CLOSE && newDistance == Referee.VERY_CLOSE -> false
        else -> true
    }

    private fun Int.toAnimationName(): String? = when (this) {
        Referee.FAR -> "far"
        Referee.CLOSE -> "close"
        Referee.VERY_CLOSE -> "very_close"
        Referee.FAR_AWAY -> "far_away"
        else -> null
    }
}