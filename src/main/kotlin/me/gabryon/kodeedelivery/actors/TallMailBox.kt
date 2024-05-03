package me.gabryon.kodeedelivery.actors

import godot.annotation.RegisterClass

@RegisterClass
class TallMailBox : MailBox() {
    override var score: Int = 300
}