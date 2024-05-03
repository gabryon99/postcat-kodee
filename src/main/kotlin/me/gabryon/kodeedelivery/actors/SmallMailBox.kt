package me.gabryon.kodeedelivery.actors

import godot.annotation.RegisterClass

@RegisterClass
class SmallMailBox : MailBox() {
    override var score: Int = 200
}