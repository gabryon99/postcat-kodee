package me.gabryon.kodeedelivery.actors

import godot.annotation.RegisterClass
import godot.signals.signal

@RegisterClass
class BottomMailbox : MailBox() {
    override var score: Int = 200
}