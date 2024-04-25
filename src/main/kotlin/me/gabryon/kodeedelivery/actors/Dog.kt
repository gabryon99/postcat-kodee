package me.gabryon.kodeedelivery.actors

import godot.*
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.Vector3
import godot.core.asStringName
import me.gabryon.kodeedelivery.managers.Scene
import me.gabryon.kodeedelivery.managers.sceneManager
import me.gabryon.kodeedelivery.utility.child

@RegisterClass
class Dog: Node3D(), Orbiting {

    @Export
    @RegisterProperty
    var rotationSpeed = Vector3(0, 90, 0)

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
        body.rotationDegrees += rotationSpeed * delta
    }

    @RegisterFunction
    override fun _physicsProcess(delta: Double) {
        orbit(delta)
    }

    @RegisterFunction
    fun onAreaEnter(area3D: Area3D) {
        // When the dog catches Kodee, the game is over.
        if (area3D.isInGroup("Player".asStringName())) {
            sceneManager.changeTo(scene = Scene.EndGame)
        }
    }
}
