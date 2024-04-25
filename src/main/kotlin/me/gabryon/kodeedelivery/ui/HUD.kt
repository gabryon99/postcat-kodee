package me.gabryon.kodeedelivery.ui

import godot.Control
import godot.Label
import godot.Tween
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.asNodePath
import me.gabryon.kodeedelivery.managers.LevelManager
import me.gabryon.kodeedelivery.managers.ScoreManager

@RegisterClass
class HUD : Control() {

    @Export
    @RegisterProperty
    lateinit var scoreManager: ScoreManager
    @Export
    @RegisterProperty
    lateinit var levelManager: LevelManager

    @Export
    @RegisterProperty
    lateinit var scoreLabel: Label
    @Export
    @RegisterProperty
    lateinit var levelLabel: Label

    @RegisterFunction
    override fun _ready() {
        scoreManager.scoreChanged.connect(this, HUD::onScoreChanged)
        scoreManager.nextLevel.connect(this, HUD::onNextLevel)
    }

    @RegisterFunction
    fun onScoreChanged(oldScore: Int, newScore: Int) {
        scoreLabel.text = String.format("%03d", newScore)
    }

    @RegisterFunction
    fun onNextLevel() {
        createTween()?.apply {
            tweenProperty(levelLabel, "modulate:a".asNodePath(), 1.0, 0.5)
                ?.setEase(Tween.EaseType.EASE_IN_OUT)
                ?.setTrans(Tween.TransitionType.TRANS_LINEAR)
            tweenProperty(levelLabel, "modulate:a".asNodePath(), 0.0, 1.0)
                ?.setEase(Tween.EaseType.EASE_IN_OUT)
                ?.setTrans(Tween.TransitionType.TRANS_LINEAR)
                ?.setDelay(0.5)
        }

    }
}