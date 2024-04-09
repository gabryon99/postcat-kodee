package me.gabryon.kodeedelivery.actors

import godot.Area3D
import godot.CSGMesh3D
import godot.Node3D
import godot.StandardMaterial3D
import godot.annotation.Export
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.annotation.RegisterSignal
import godot.core.Color
import godot.core.asStringName
import godot.extensions.getNodeAs
import godot.signals.signal
import me.gabryon.kodeedelivery.managers.Scorable
import kotlin.apply

abstract class MailBox : Node3D(), Scorable {

    @Export
    @RegisterProperty
    lateinit var deliveredColor: Color // This is a debug property!

    lateinit var kodeeOrbit: Node3D

    @RegisterSignal
    override val scored by signal<Int>("points")

    @RegisterFunction
    override fun _ready() {
        val currentScene = getTree()!!.currentScene!!
        kodeeOrbit = currentScene.getNodeAs<Node3D>("%KodeeOrbitPoint".asStringName())!!
        val area3D = findChild("MailboxArea", recursive = true) as Area3D
        area3D.areaEntered.connect(this, MailBox::onBodyEntered)
    }

    @RegisterFunction
    fun onBodyEntered(body: Node3D) {

        if (body.isInGroup("Player".asStringName())) {

            scored.emit(score)

            val mailbox = findChild("Model") as CSGMesh3D
            mailbox.material = StandardMaterial3D().apply {
                albedoColor = deliveredColor
            }
        }
    }
}