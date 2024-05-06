package me.gabryon.kodeedelivery.actors

import godot.*
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.Vector3
import godot.core.asStringName
import me.gabryon.kodeedelivery.managers.Scene
import me.gabryon.kodeedelivery.managers.ScoreStorage
import me.gabryon.kodeedelivery.managers.sceneManager
import me.gabryon.kodeedelivery.utility.child

@RegisterClass
class Dog: Node3D(), Orbiting {

    @Export
    @RegisterProperty
    var rotationSpeed = Vector3(0, 90, 0)

    @Export
    @RegisterProperty
    lateinit var camera: Camera3D

    private val body by child<Area3D>("Body")

    override var angularSpeed: Double = 0.0
    override var initialAngularSpeed: Double = 1.0
    override var maximumAngularSpeed: Double = 1.0
    override var deltaSpeed: Double = 1.0

    @RegisterFunction
    override fun _ready() {
        body.areaEntered.connect(this, Dog::onAreaEnter)
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        // body.rotationDegrees += rotationSpeed * delta
        val up = (globalPosition - (getParent()!! as Node3D).globalPosition)
        lookAt(camera.globalPosition, up.normalized(), useModelFront = true)
    }

    @RegisterFunction
    override fun _physicsProcess(delta: Double) {
        orbit(delta)
    }

    @RegisterFunction
    fun onAreaEnter(area3D: Area3D) {
        // When the dog catches Kodee, the game is over.
        // Save maximum user score and change to the next scene
        if (area3D.isInGroup(Kodee.GROUP_NAME.asStringName())) {
            ScoreStorage.saveUserScoreToDevice()
            sceneManager.changeTo(scene = Scene.EndGame)
        }
    }
}
