package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.asNodePath
import me.gabryon.kodeedelivery.annotations.Autoload

enum class Scene(val scenePath: String) {
    StartGame("scenes/start-scene.tscn"),
    MainGame("scenes/main.tscn"),
    EndGame("scenes/end-scene.tscn")
}

@Autoload
@RegisterClass
class SceneManager : Node() {
    @RegisterFunction
    fun changeTo(scene: Scene) {
        getTree()?.changeSceneToFile(scene.scenePath)
    }
}

val Node.sceneManager: SceneManager
    get() {
        return getNode("/root/SceneManagement".asNodePath()) as SceneManager
    }