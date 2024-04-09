package me.gabryon.kodeedelivery.actors

import godot.Area3D
import godot.Input.isActionJustReleased
import godot.Node3D
import godot.Timer
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.annotation.RegisterSignal
import godot.core.asStringName
import godot.global.GD
import godot.signals.signal

@RegisterClass
class Kodee : Area3D(), Orbiting {

    enum class HorizontalMovePoint {
        LEFT, CENTER, RIGHT;

        fun left(): HorizontalMovePoint = when (this) {
            LEFT -> RIGHT
            CENTER -> LEFT
            RIGHT -> CENTER
        }

        fun right(): HorizontalMovePoint = when (this) {
            LEFT -> CENTER
            CENTER -> RIGHT
            RIGHT -> LEFT
        }
    }

    enum class VerticalMovePoint {
        UP, DOWN;
    }

    //region Reset Timer Fields
    @Export
    @RegisterProperty
    lateinit var resetPositionTimer: Timer
    @Export
    @RegisterProperty
    var resetHorizontalPositionTime: Double = 0.5
    @Export
    @RegisterProperty
    var resetVerticalPositionTime: Double = 1.0
    //endregion

    //region Moving Point Fields
    @Export
    @RegisterProperty
    lateinit var upMovePoint: Node3D
    @Export
    @RegisterProperty
    lateinit var downMovePoint: Node3D
    @Export
    @RegisterProperty
    lateinit var leftMovePoint: Node3D
    @Export
    @RegisterProperty
    lateinit var centerMovePoint: Node3D
    @Export
    @RegisterProperty
    lateinit var rightMovePoint: Node3D

    val rotationY: Double
        get() = (getParent() as Node3D).rotation.y

    private lateinit var horizontalMovePoints: List<Node3D>
    private lateinit var verticalMovePoints: List<Node3D>
    private var currentHorizontalPoint = HorizontalMovePoint.CENTER
    private var currentVerticalPoint = VerticalMovePoint.DOWN
    //endregion

    //region Orbiting Fields
    override var angularSpeed: Double = 0.0
    override var initialAngularSpeed: Double = 5.0
    override var maximumAngularSpeed: Double = 10.0
    override var deltaSpeed: Double = 1.0
    //endregion

    @RegisterSignal
    val comboChanged by signal<Int>("delta")
    @RegisterSignal
    val comboLost by signal()

    private var mailboxTouched: Boolean = false

    @RegisterFunction
    override fun _ready() {
        // Initialize vertical points
        verticalMovePoints = listOf(upMovePoint, downMovePoint)
        // Initialize horizontal move points
        horizontalMovePoints = listOf(leftMovePoint, centerMovePoint, rightMovePoint)

        // Collisions with mailboxes
        areaEntered.connect(this, Kodee::onAreaEnter)
        areaExited.connect(this, Kodee::onAreaExit)

        // Timers and other funny stuff
        resetPositionTimer.timeout.connect(this, Kodee::onResetPositionTimeout, 0)
    }

    @RegisterFunction
    override fun _physicsProcess(delta: Double) {
        orbit(delta)

        if (isActionJustReleased("left".asStringName())) {
            updateHorizontalPosition(currentHorizontalPoint.left(), reset = true)
        }
        if (isActionJustReleased("right".asStringName())) {
            updateHorizontalPosition(currentHorizontalPoint.right(), reset = true)
        }
        if (isActionJustReleased("up".asStringName())) {
            updateVerticalPosition(VerticalMovePoint.UP, reset = true)
        }
        if (isActionJustReleased("down".asStringName())) {
            updateVerticalPosition(VerticalMovePoint.DOWN, reset = true)
        }
    }

    private fun updateVerticalPosition(movePoint: VerticalMovePoint, reset: Boolean = false) {
        val movePointNode = verticalMovePoints[movePoint.ordinal]
        position = positionMutate {
            y = movePointNode.position.y
            z = movePointNode.position.z
        }

        if (reset && currentVerticalPoint != movePoint) {
            resetPositionTimer.start(resetVerticalPositionTime)
        }
        if (reset && resetPositionTimer.timeLeft > 0) {
            resetPositionTimer.stop()
            resetPositionTimer.start(resetVerticalPositionTime)
        }

        currentVerticalPoint = movePoint
    }

    private fun updateHorizontalPosition(movePoint: HorizontalMovePoint, reset: Boolean = false) {
        val movePointNode = horizontalMovePoints[movePoint.ordinal]
        position = positionMutate {
            x = movePointNode.position.x
        }

        // Did Kodee actually move from its position?
        if (reset && currentHorizontalPoint != movePoint) {
            resetPositionTimer.start(resetHorizontalPositionTime)
        }
        if (reset && resetPositionTimer.timeLeft > 0) {
            resetPositionTimer.stop()
            resetPositionTimer.start(resetHorizontalPositionTime)
        }

        currentHorizontalPoint = movePoint
    }

    private val skipCollidingAreas = mutableSetOf<Area3D>()

    @RegisterFunction
    fun onAreaExit(body: Area3D) {
        // We deregister only skip area from the `collidingSkipArea`s
        if (!body.isInGroup("MailboxSkip".asStringName())) return

        // Remove skip area from the set
        skipCollidingAreas.remove(body)

        // Did Kodee touch at least one mailbox?
        if (skipCollidingAreas.isEmpty() && !mailboxTouched) {
            comboLost.emit()
            decelerate()
            GD.prints("[info][kodee] :: kodee didn't score anything")
        }

        if (skipCollidingAreas.isEmpty() && mailboxTouched) {
            mailboxTouched = false
        }
    }

    @RegisterFunction
    fun onAreaEnter(body: Area3D) {
        when {
            body.isInGroup("MailboxSkip".asStringName()) -> {
                skipCollidingAreas.add(body)
            }
            body.isInGroup("Mailbox".asStringName()) -> {
                mailboxTouched = true
                comboChanged.emit(1)
                accelerate(maxSpeed = maximumAngularSpeed)
            }
        }
    }

    @RegisterFunction
    fun onResetPositionTimeout() {
        // Reset Kodee's position to original
        updateVerticalPosition(VerticalMovePoint.DOWN, reset = false)
        updateHorizontalPosition(HorizontalMovePoint.CENTER, reset = false)
    }

}