package me.gabryon.kodeedelivery.ui

import godot.*
import godot.annotation.*
import godot.core.Vector2

@Tool
@RegisterClass
class TiltedEffect : RichTextEffect() {
    @Export
    @RegisterProperty
    var bbcode = "tilt"

    init {
        resourceName = "TiltedEffect"
    }

    @RegisterFunction
    override fun _processCustomFx(charFx: CharFXTransform): Boolean {
        if (charFx.relativeIndex % 2 == 0) return false

        charFx.offset = Vector2(charFx.env["offset_x"] as Double, charFx.env["offset_y"] as Double)
        charFx.transform = charFx.transform.rotatedLocal(0.1329941) // 7.62 deg

        return true
    }
}

@RegisterClass
class ScoreLabel : RichTextLabel() {
    @RegisterFunction
    fun onScoreChanged(score: Int) {
        parseBbcode("[center][tilt offset_x=-6.0 offset_y=8.0]$score[/tilt][/center]")
    }
}