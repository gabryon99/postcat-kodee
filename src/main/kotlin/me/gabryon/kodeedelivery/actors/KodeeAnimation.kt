package me.gabryon.kodeedelivery.actors

import ch.hippmann.godot.utilities.logging.debug
import godot.AnimationNodeStateMachinePlayback
import godot.AnimationTree
import godot.annotation.RegisterClass
import godot.annotation.RegisterProperty
import godot.core.StringName
import godot.core.asStringName
import me.gabryon.kodeedelivery.managers.Referee

@RegisterClass
class KodeeAnimation : AnimationTree() {

    @RegisterProperty
    var nextAnimation = Referee.FAR_AWAY.asStringName()

    fun changeAnimation(animationName: StringName) {
        debug("Changing animation to: $animationName")
        val stateMachine = get("parameters/playback".asStringName()) as AnimationNodeStateMachinePlayback
        stateMachine.travel(animationName)
    }

    fun onDistanceWithDogChanged(distance: StringName) {
        // If we are stretched, we should save the new animation
        // temporarily to put it back when Kodee goes down.
        nextAnimation = distance
    }
}