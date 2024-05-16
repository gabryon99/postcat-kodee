package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.Node3D
import godot.annotation.*
import godot.core.Quaternion
import godot.core.Vector3
import godot.global.GD
import godot.signals.signal
import me.gabryon.kodeedelivery.actors.Dog
import me.gabryon.kodeedelivery.actors.Kodee
import me.gabryon.kodeedelivery.actors.accelerate
import me.gabryon.kodeedelivery.utility.debugLine

// Only Wout and Gabriele know what happen here on 5th of April 2024.
@RegisterClass
class Referee : Node() {

    companion object {
        const val FAR_AWAY = 0
        const val FAR = 1
        const val CLOSE = 2
        const val VERY_CLOSE = 3
    }

    @RegisterSignal
    val distanceChanged by signal<Int>("distance")

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
    lateinit var kodee: Kodee

    @Export
    @RegisterProperty
    lateinit var dog: Dog

    private var currentDistance = FAR_AWAY

    @RegisterFunction
    override fun _ready() {
        // At the beginning of the game, Kodee's position must be the same as the one in the editor.
        // However, we want to keep abs(kodee.pos - dog.pos) <= Vector.ONE * maxDogDistanceDogAndKodee true
        val currentKodeeEditorPosition = kodeeOrbitPoint.quaternion
        // The dog should always fall behind Kodee, let's project how much distant
        val distanceFromKodee = Quaternion(Vector3.RIGHT, -maxDistanceDogAndKodee)
        // The dog's position is given by the quaternion sum of Kodee and the projected distance from him.
        dogOrbitPoint.quaternion = distanceFromKodee * currentKodeeEditorPosition
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        debugLine(silent = true) {

            val currentDistance by dogOrbitPoint.quaternion.angleTo(kodeeOrbitPoint.quaternion).also {
                // Only emit the `distanceChanged` signal when the actual distance is different
                // from the previous frame.
                val newDistance = it.toDistance()
                if (newDistance != currentDistance) {
                    distanceChanged.emit(newDistance)
                    currentDistance = newDistance
                }
            }

            val x by currentDistance / maxDistanceDogAndKodee
            val y by dogSpeedFreedomCurve(x)

            val speedDiff by (dog.angularSpeed - kodee.angularSpeed + overtakeSpeed)

            val dogIsCloseToKodee by x < 2.0 / 3.0

            val kodeeIsFasterThanDog = speedDiff > 0
            val kodeeMadeProgressWhileDogWasClose = dogIsCloseToKodee && kodeeIsFasterThanDog

            if (!kodeeMadeProgressWhileDogWasClose) {
                val y2 by dogMaxSpeedCurve(x)
                dog.accelerate(delta = speedDiff * y, maxSpeed = y2)
            }
        }
    }

    /**
     * x \in [0, 1]
     * x==1 when distanceDogKodee==1.6
     */
    private fun dogMaxSpeedCurve(x: Double): Double =
        GD.pow(x, dogMaxSpeedCurviness) * (kodee.maximumAngularSpeed - minMaxSpeedDog) + minMaxSpeedDog

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

    private fun Double.toDistance(): Int {
        return when (this) {
            in 1.5..Double.MAX_VALUE -> FAR_AWAY
            in 1.0..1.5 -> FAR
            in 0.4..1.0 -> CLOSE
            else -> VERY_CLOSE
        }
    }
}