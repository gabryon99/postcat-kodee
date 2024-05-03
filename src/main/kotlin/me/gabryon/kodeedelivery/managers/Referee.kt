package me.gabryon.kodeedelivery.managers

import ch.hippmann.godot.utilities.logging.info
import godot.Node
import godot.Node3D
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.global.GD
import me.gabryon.kodeedelivery.actors.Dog
import me.gabryon.kodeedelivery.actors.Kodee
import me.gabryon.kodeedelivery.actors.accelerate
import me.gabryon.kodeedelivery.ui.Indicator
import me.gabryon.kodeedelivery.utility.absoluteAngularDistance

// Only Wout and Gabriele know what happen here on 5th of April 2024.
@RegisterClass
class Referee : Node() {

    @Export
    @RegisterProperty
    lateinit var kodeeOrbitPoint: Node3D

    @Export
    @RegisterProperty
    lateinit var dogOrbitPoint: Node3D

    @Export
    @RegisterProperty
    var maxDistanceDogAndKodee: Double = 1.0

    @Export
    @RegisterProperty
    lateinit var indicator: Indicator

    @Export
    @RegisterProperty
    var bounciness: Double = 32.0

    @Export
    @RegisterProperty
    var dogMaxSpeedCurviness: Double = 2.0

    @Export
    @RegisterProperty
    var minMaxSpeedDog: Double = 2.0

    @Export
    @RegisterProperty
    var overtakeSpeed: Double = 0.1

    @Export
    @RegisterProperty
    lateinit var kodeeScript: Kodee

    @Export
    @RegisterProperty
    lateinit var dogScript: Dog

    @RegisterFunction
    override fun _process(delta: Double) {

        val kodeeAngle = kodeeOrbitPoint.rotation.x
        val dogAngle = dogOrbitPoint.rotation.x
        val currentDistance = absoluteAngularDistance(dogAngle, kodeeAngle)

        val x = currentDistance / maxDistanceDogAndKodee
        val y = dogSpeedFreedomCurve(x)

        val deltaSpeedDiff = (dogScript.angularSpeed - kodeeScript.angularSpeed + overtakeSpeed)

        val dogIsCloseToKodee = x < 2.0 / 3.0
        val kodeeIsFasterThanDog = deltaSpeedDiff > 0
        val kodeeMadeProgressWhileDogWasClose = dogIsCloseToKodee && kodeeIsFasterThanDog

        if (!kodeeMadeProgressWhileDogWasClose) {
            val y2 = dogMaxSpeedCurve(x)
            dogScript.accelerate(delta = deltaSpeedDiff * y, maxSpeed = y2)
        }

        indicator.updateBar(currentDistance * 100)
//
//        info(
//            "current distance=$currentDistance, " +
//                    "max distance=$maxDistanceDogAndKodee, " +
//                    "kodee's angle=$kodeeAngle, " +
//                    "dog's angle=$dogAngle"
//        )
//        info(
//            "dF=$deltaSpeedDiff, " +
//                    "totalCond=${!(dogIsCloseToKodee && kodeeIsFasterThanDog)}, " +
//                    "dogIsCloseToKodee=$dogIsCloseToKodee, " +
//                    "kodeeIsFasterThanDog=$kodeeIsFasterThanDog, " +
//                    "x=$x, y=$y, " +
//                    "dog's speed=${dogScript.angularSpeed}, " +
//                    "kodee's speed=${kodeeScript.angularSpeed}"
//        )
    }

    private fun dogMaxSpeedCurve(x: Double): Double =
        GD.pow(x, dogMaxSpeedCurviness) * (kodeeScript.maximumAngularSpeed - minMaxSpeedDog) + minMaxSpeedDog

    /**
     * This function of x, gives a point on a curve that looks like:
     * ```text
     *  1 _____   __
     *  0    | \_/
     * -1
     * ```
     * Here you should imagine `|` being Kodee, and the further the dog is from Kodee,
     * the further to the right it is in this graph.
     *
     * The lower the result of this function,
     * the more constrained the dog's movement is to Kodee's speed + overtake speed.
     *
     * The reason we use this is to make sure that the user's actions seem very visible,
     * if you make progress,
     * the ball quickly moves back,
     * and if you make mistakes,
     * the ball will come towards you quickly.
     *
     * But the speed of the dog is constrained when:
     *  - It's close to the user,
     *    so they don't lose too fast when they make mistakes.
     *  - It's far from the user,
     *    so the dog does exit the frame.
     *
     *  However, sometimes we lift this restriction when the dog is close, and Kodee just made progress.
     */
    private fun dogSpeedFreedomCurve(x: Double): Double =
        GD.pow(GD.clamp(3 * x - 2, -1.0, 1.0), bounciness)
}