package me.gabryon.kodeedelivery.ui

import godot.annotation.RegisterClass
import me.gabryon.kodeedelivery.managers.Scene

@RegisterClass
class OpenAboutButton : ChangeSceneButtonLogic(
    sceneToLoad = Scene.About,
)