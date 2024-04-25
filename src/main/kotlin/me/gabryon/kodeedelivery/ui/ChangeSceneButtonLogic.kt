package me.gabryon.kodeedelivery.ui

import godot.Button
import godot.Tween
import godot.annotation.RegisterFunction
import godot.core.asNodePath
import me.gabryon.kodeedelivery.managers.Scene
import me.gabryon.kodeedelivery.managers.sceneManager

/**
 * Abstract class representing the logic for a button that changes scenes when pressed.
 *
 * @param sceneToLoad The scene to load when the button is pressed.
 * @param onFadesOutEnd Function to be called when the fade-out animation ends.
 */
abstract class ChangeSceneButtonLogic(
    private val sceneToLoad: Scene,
    private val onFadesOutEnd: () -> Unit = {}
) : Button() {

    @RegisterFunction
    override fun _ready() {
        pressed.connect(this, ChangeSceneButtonLogic::onButtonClick, 0)
    }

    @RegisterFunction
    fun onButtonClick() {
        createTween()
            ?.tweenProperty(getParent()!!, "modulate:a".asNodePath(), 0.0, 1.0)
            ?.setEase(Tween.EaseType.EASE_OUT_IN)
            ?.setTrans(Tween.TransitionType.TRANS_LINEAR)
            ?.finished
            ?.connect(this, ChangeSceneButtonLogic::changeScene)
    }

    @RegisterFunction
    fun changeScene() {
        onFadesOutEnd()
        sceneManager.changeTo(sceneToLoad)
    }
}