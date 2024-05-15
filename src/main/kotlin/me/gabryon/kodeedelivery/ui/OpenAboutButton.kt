package me.gabryon.kodeedelivery.ui

import godot.Button
import godot.OS
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import me.gabryon.kodeedelivery.managers.Scene
import me.gabryon.kodeedelivery.utility.ABOUT_LINK

@RegisterClass
class OpenAboutButton : Button() {
    @RegisterFunction
    fun openAboutLink() {
        OS.shellOpen(ABOUT_LINK)
    }
}