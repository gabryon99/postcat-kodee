package me.gabryon.kodeedelivery.managers

import godot.Camera2D
import godot.Input
import godot.annotation.*
import godot.core.Vector2
import godot.core.asStringName
import godot.signals.signal
import kotlin.math.abs

@RegisterClass
class GestureManager : Camera2D() {

    @Export
    @RegisterProperty
    var length = 50.0

    @Export
    @RegisterProperty
    var threshold = 10

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

    private fun dispatchHorizontalSignal(diff: Vector2) {
        if (diff.x < 0) {
            onSwipeToLeft.emit()
        }
        else if (diff.x > 0) {
            onSwipeToRight.emit()
        }
    }

    private fun dispatchVerticalSignal(diff: Vector2) {
        if (diff.y < 0) {
            onSwipeUp.emit()
        }
        else if (diff.y > 0) {
            onSwipeDown.emit()
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
                if (abs(startPos.y - currentPos.y) <= threshold) {
                    dispatchHorizontalSignal(currentPos - startPos)
                    swiping = false
                }
                else if (abs(startPos.x - currentPos.x) <= threshold) {
                    dispatchVerticalSignal(currentPos - startPos)
                    swiping = false
                }

            }
        } else {
            swiping = false
        }
    }
}