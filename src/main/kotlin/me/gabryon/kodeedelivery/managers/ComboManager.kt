package me.gabryon.kodeedelivery.managers

import godot.Node
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.asStringName
import godot.extensions.getNodeAs
import me.gabryon.kodeedelivery.actors.Kodee
import kotlin.Int.Companion.MAX_VALUE
import kotlin.ranges.coerceIn
import kotlin.run

@RegisterClass
class ComboManager : Node() {

    companion object {
        const val DEFAULT_MULTIPLIER = 1.0
    }

    private var comboCounter: Int = 0
    private var maxComboCounterReached: Int = 0

    @Export
    @RegisterProperty
    var maximumMultiplier: Double = 2.0
    @Export
    @RegisterProperty
    var multiplierIncrease: Double = 0.1

    @Export
    @RegisterProperty
    lateinit var kodee: Kodee

    var currentComboMultiplier: Double = DEFAULT_MULTIPLIER
        private set

    @RegisterFunction
    override fun _ready() {
        kodee.run {
            comboChanged.connect(this@ComboManager, ComboManager::onComboChanged)
            comboLost.connect(this@ComboManager, ComboManager::onComboLost)
        }
    }

    @RegisterFunction
    fun onComboChanged(delta: Int) {
        // Increase the multiplier...
        currentComboMultiplier = (currentComboMultiplier + multiplierIncrease).coerceAtMost(maximumMultiplier)
        comboCounter = (comboCounter + delta).coerceIn(0..MAX_VALUE)
        if (comboCounter > maxComboCounterReached) {
            maxComboCounterReached = comboCounter
        }
    }

    /**
     * When the combo is lost, reset the multiplier to its default value,
     * and the combo counter back to zero.
     */
    @RegisterFunction
    fun onComboLost() {
        comboCounter = 0
        currentComboMultiplier = DEFAULT_MULTIPLIER
    }

}