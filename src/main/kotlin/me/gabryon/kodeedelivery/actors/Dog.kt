package me.gabryon.kodeedelivery.actors

import godot.Area3D
import godot.CSGSphere3D
import godot.StandardMaterial3D
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.Color.Companion.red
import godot.core.Vector3
import godot.core.asStringName
import kotlin.apply

@RegisterClass
class Dog: Area3D(), Orbiting {

    @Export
    @RegisterProperty
    var rotationSpeed = Vector3(0, 90, 0)

    @RegisterFunction
    override fun _process(delta: Double) {
        rotationDegrees += rotationSpeed * delta
    }

    override var angularSpeed: Double = 0.0
    override var initialAngularSpeed: Double = 1.0
    override var maximumAngularSpeed: Double = 1.0
    override var deltaSpeed: Double = 1.0

    @RegisterFunction
    override fun _ready() {
        areaEntered.connect(this, Dog::onAreaEnter)
    }

    @RegisterFunction
    override fun _physicsProcess(delta: Double) {
        orbit(delta)
    }

    @RegisterFunction
    fun onAreaEnter(area3D: Area3D) {
        if (area3D.isInGroup("Player".asStringName())) {
            val sphere = findChild("CSGSphere3D")!! as CSGSphere3D
            sphere.material = StandardMaterial3D().apply {
                albedoColor = red
            }
        }
    }
}
