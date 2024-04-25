package me.gabryon.kodeedelivery.ui

import godot.annotation.RegisterClass
import me.gabryon.kodeedelivery.managers.Scene
import me.gabryon.kodeedelivery.managers.ScoreStorage

@RegisterClass
class StartGameButton : ChangeSceneButtonLogic(
    sceneToLoad = Scene.MainGame,
    onFadesOutEnd = {
        ScoreStorage.resetScore()
    }
)