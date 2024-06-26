package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.Node3D
import godot.VisibleOnScreenNotifier3D
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.Vector3
import me.gabryon.kodeedelivery.actors.Dog
import me.gabryon.kodeedelivery.actors.Kodee

@RegisterClass
class EndGameManager : Node() {

    @Export
    @RegisterProperty
    lateinit var kodeeOrbitPoint: Node3D

    @Export
    @RegisterProperty
    lateinit var kodee: Kodee

    @Export
    @RegisterProperty
    lateinit var dog: Dog

    @Export
    @RegisterProperty
    lateinit var world: Node3D

    @Export
    @RegisterProperty
    var flightSpeed = 2.0

    @Export
    @RegisterProperty
    var flightDirection = Vector3(1.0, 1.0, 0.0)

    private var gameHasEnded = false

    @RegisterFunction
    fun onDogColliding() {
        ScoreStorage.saveUserScoreToDevice()
        stopKodeeAndDog()
        setKodeeNotifier()
        reparentKodeeOrbitPointChildren()
        dog.barkAndJump()
        kodee.flyAway()
        gameHasEnded = true
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        if (!gameHasEnded) return
        kodee.position += (flightDirection.times(delta * flightSpeed))
    }

    private fun stopKodeeAndDog() {
        dog.angularSpeed = 0.0
        kodee.angularSpeed = 0.0
    }

    private fun reparentKodeeOrbitPointChildren() {
        kodeeOrbitPoint.getChildren().forEach {
            it.reparent(world)
        }
    }

    private fun setKodeeNotifier() {
        val screenNotifier = kodee.findChild("VisibleOnScreenNotifier3D") as VisibleOnScreenNotifier3D
        screenNotifier.screenExited.connect(this, EndGameManager::onKodeeNotVisibleAnymore)
    }

    @RegisterFunction
    fun onKodeeNotVisibleAnymore() {
        sceneManager.changeTo(scene = Scene.EndGame)
    }
}