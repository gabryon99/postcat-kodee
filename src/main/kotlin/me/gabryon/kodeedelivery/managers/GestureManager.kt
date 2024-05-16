package me.gabryon.kodeedelivery.managers

import godot.Camera2D
import godot.Input
import godot.annotation.*
import godot.core.Vector2
import godot.core.asStringName
import godot.global.GD
import godot.signals.signal
import kotlin.math.atan2

@RegisterClass
class GestureManager : Camera2D() {

    @Export
    @RegisterProperty
    var length = 50.0

    private var startPos = Vector2.ZERO
    private var currentPos = Vector2.ZERO
    private var swiping = false

    @RegisterSignal
    val onSwipeToLeft by signal()

    @RegisterSignal
    val onSwipeToRight by signal()

    @RegisterSignal
    val onSwipeUp by signal()

    @RegisterSignal
    val onSwipeDown by signal()

    private fun getSwipeAngle(start: Vector2, end: Vector2): Double {
        val diff = end - start
        val rad = atan2(diff.y, diff.x)
        var deg = GD.radToDeg(rad)
        if (deg < 0) deg += 360.0
        return deg
    }

    private fun dispatchSignal(start: Vector2, end: Vector2) {
        val angle = getSwipeAngle(start, end)
        when {
            (angle in 315.0..360.0) || (angle in 0.0..45.0) -> onSwipeToRight.emit()
            (angle in 45.0..135.0) -> onSwipeDown.emit()
            (angle in 135.0..225.0) -> onSwipeToLeft.emit()
            (angle in 225.0..315.0) -> onSwipeUp.emit()
        }
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        if (Input.isActionJustPressed("pressed".asStringName()) && !swiping) {
            swiping = true
            startPos = getGlobalMousePosition()
        }
        if (Input.isActionPressed("pressed".asStringName()) && swiping) {
            currentPos = getGlobalMousePosition()
            if (startPos.distanceTo(currentPos) >= length) {
                dispatchSignal(startPos, currentPos)
                swiping = false
            }
        } else {
            swiping = false
        }
    }
}