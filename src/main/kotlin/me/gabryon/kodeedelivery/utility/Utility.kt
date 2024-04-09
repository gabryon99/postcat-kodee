package me.gabryon.kodeedelivery.utility

import godot.util.PI
import godot.global.GD.abs as abs
import godot.global.GD.fmod as fmod

const val DPI = PI * 2

fun absoluteAngularDistance(from: Double, to: Double): Double {
    val angle1 = fmod(from, DPI)
    val angle2 = fmod(to, DPI)
    val diff = abs(angle1 - angle2)
    return when {
        diff > PI -> DPI - diff
        else -> diff
    }
}

fun angularDistance(from: Double, to: Double): Double {
    val angle1 = fmod(from, DPI)
    val angle2 = fmod(to, DPI)
    val diff = angle2 - angle1
    return when {
        diff > PI -> diff - DPI
        diff < -PI -> diff + DPI
        else -> diff
    }
}