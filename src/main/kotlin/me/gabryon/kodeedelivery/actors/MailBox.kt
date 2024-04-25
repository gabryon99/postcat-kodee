package me.gabryon.kodeedelivery.actors

import godot.*
import godot.annotation.Export
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.annotation.RegisterSignal
import godot.core.Color
import godot.core.asStringName
import godot.extensions.getNodeAs
import godot.signals.signal
import me.gabryon.kodeedelivery.managers.Scorable
import me.gabryon.kodeedelivery.utility.child

abstract class MailBox : Node3D(), Scorable {

    @Export
    @RegisterProperty
    lateinit var deliveredColor: Color // This is a debug property!

    private lateinit var kodeeOrbit: Node3D

    @RegisterSignal
    override val scored by signal<Int>("points")

    private val letterboxSound by child<AudioStreamPlayer3D>("LetterboxSound")

    @RegisterFunction
    override fun _ready() {
        val currentScene = getTree()!!.currentScene!!
        kodeeOrbit = currentScene.getNodeAs("%KodeeOrbitPoint".asStringName())!!
        val area3D = findChild("MailboxArea", recursive = true) as Area3D
        area3D.areaEntered.connect(this, MailBox::onBodyEntered)
    }

    @RegisterFunction
    fun onBodyEntered(body: Node3D) {
        if (body.isInGroup("Player".asStringName())) {
            letterboxSound.play()
            // Signal new score!
            scored.emit(score)
        }
    }
}