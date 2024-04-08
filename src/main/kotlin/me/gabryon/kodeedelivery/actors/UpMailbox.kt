package me.gabryon.kodeedelivery.actors

import godot.annotation.RegisterClass
import godot.signals.signal

@RegisterClass
class UpMailbox : MailBox() {
    override var score: Int = 300
}