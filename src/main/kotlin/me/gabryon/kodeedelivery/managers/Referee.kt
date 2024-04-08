package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.Node3D
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.asStringName
import godot.extensions.getNodeAs
import godot.global.GD
import me.gabryon.kodeedelivery.actors.Dog
import me.gabryon.kodeedelivery.actors.Kodee
import me.gabryon.kodeedelivery.actors.accelerate
import me.gabryon.kodeedelivery.utility.angleDistance
import me.gabryon.kodeedelivery.utility.debugContext

// Only Wout and Gabriele know what happen here on 5th of April 2024.
@RegisterClass
class Referee : Node() {

    @Export
    @RegisterProperty
    lateinit var kodee: Node3D

    @Export
    @RegisterProperty
    lateinit var dog: Node3D

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

    lateinit var kodeeScript: Kodee
    lateinit var dogScript: Dog

    @RegisterFunction
    override fun _ready() {
        kodeeScript = getNodeAs<Kodee>("%Kodee".asStringName())!!
        dogScript = getNodeAs<Dog>("%Dog".asStringName())!!
    }

    @RegisterFunction
    override fun _process(delta: Double) {

        val kodeeAngle = kodee.rotation.y
        val dogAngle = dog.rotation.y
        val currentDistance = angleDistance(dog.rotation.y, kodee.rotation.y)

        val x = (currentDistance / maxDistanceDogAndKodee)
        val y = GD.pow(GD.clamp(3 * x - 2, -1.0, 1.0), bounciness)

        val deltaSpeedDiff = (dogScript.angularSpeed - kodeeScript.angularSpeed - overtakeSpeed)

        val cond1 = x < 2.0 / 3.0
        val cond2 = deltaSpeedDiff > 0

        if (!(cond1 && cond2)) {
            val y2 = GD.pow(x, dogMaxSpeedCurviness) * (kodeeScript.maximumAngularSpeed - minMaxSpeedDog) + minMaxSpeedDog
            dogScript.accelerate(deltaSpeedDiff * y, y2)
        }

        debugContext {
//            info<Referee>(
//                "current distance = $currentDistance, " +
//                        "max distance = $maxDistanceDogAndKodee, " +
//                        "kodee's angle=$kodeeAngle, " +
//                        "dog's angle=$dogAngle"
//            )
//            info<Referee>(
//                "dF=$deltaSpeedDiff, " +
//                        "totalCond=${!(cond1 && cond2)}, " +
//                        "cond1=$cond1, " +
//                        "cond2=$cond2, " +
//                        "x=$x, y=$y, " +
//                        "dog's speed = ${dogScript.angularSpeed}, " +
//                        "kodee's speed = ${kodeeScript.angularSpeed}"
//            )
        }
    }
}