package me.gabryon.kodeedelivery.actors

import godot.*
import godot.Input.isActionJustReleased
import godot.annotation.*
import godot.core.StringName
import godot.core.asNodePath
import godot.core.asStringName
import godot.signals.signal
import me.gabryon.kodeedelivery.utility.child

@RegisterClass
class Kodee : Area3D(), Orbiting {

    companion object {
        const val GROUP_NAME = "Player"
        private const val HEIGHT_DELTA = 0.322
    }

    enum class HorizontalMovePoint { LEFT, CENTER, RIGHT }

    private val animationTree by child<KodeeAnimation>("AnimationTree")
    private val resetPositionTimer by child<Timer>("ResetPositionTimer")
    private val resetStretchTimer by child<Timer>("ResetStretchTimer")

    @Export
    @RegisterProperty
    var resetHorizontalPositionTime = 0.5

    /**
     * Kodee cannot be stretched for an infinite amount of time.
     * Therefore, after a [stretchTimeout] seconds, go back to normal.
     */
    @Export
    @RegisterProperty
    var stretchTimeout = 1.0

    //region Moving Point Fields
    @Export
    @RegisterProperty
    lateinit var leftMovePoint: Node3D

    @Export
    @RegisterProperty
    lateinit var centerMovePoint: Node3D

    @Export
    @RegisterProperty
    lateinit var rightMovePoint: Node3D
    private lateinit var horizontalMovePoints: Array<Node3D>
    private var currentHorizontalPoint = HorizontalMovePoint.CENTER
    //endregion

    //region Orbiting Fields
    override var angularSpeed: Double = 0.0
    override var initialAngularSpeed: Double = 5.0
    override var maximumAngularSpeed: Double = 10.0
    override var deltaSpeed: Double = 1.0

    val parentRotationX: Double
        get() {
            val parent = getParent() as Node3D
            return parent.rotation.x
        }
    //endregion

    @RegisterSignal
    val comboLost by signal()

    @RegisterSignal
    val comboChanged by signal<Int>("delta")

    private var mailboxTouched = false
    private var isStretched = false

    private val swooshSound by child<AudioStreamPlayer3D>("SwooshSound")
    private val stretchSound by child<AudioStreamPlayer3D>("StretchSound")
    private val skipCollidingAreas = mutableSetOf<Area3D>()

    private lateinit var parent: Node3D

    @RegisterFunction
    override fun _ready() {
        parent = getParent() as Node3D
        horizontalMovePoints = arrayOf(leftMovePoint, centerMovePoint, rightMovePoint)
        // Collisions with mailboxes
        areaEntered.connect(this, Kodee::onAreaEnter)
        areaExited.connect(this, Kodee::onAreaExit)
    }

    @RegisterFunction
    override fun _physicsProcess(delta: Double) {
        orbit(delta)
        when {
            isActionJustReleased("left".asStringName()) -> {
                updateHorizontalPosition(HorizontalMovePoint.LEFT, reset = true)
            }

            isActionJustReleased("right".asStringName()) -> {
                updateHorizontalPosition(HorizontalMovePoint.RIGHT, reset = true)
            }

            isActionJustReleased("up".asStringName()) -> {
                stretchUp()
            }

            isActionJustReleased("down".asStringName()) -> {
                stretchDown()
            }
        }
    }

    @RegisterFunction
    fun onSwipeToLeft() {
        updateHorizontalPosition(HorizontalMovePoint.LEFT, reset = true)
    }
        @RegisterFunction
    fun onSwipeToRight() {
        updateHorizontalPosition(HorizontalMovePoint.RIGHT, reset = true)
    }

    @RegisterFunction
    fun stretchUp() {
        // We already stretched up, so we do nothing...
        if (isStretched) return

        isStretched = true
        resetStretchTimer.start(stretchTimeout)
        stretchSound.play()

        // Run animations
        animationTree.changeAnimation("stretching".asStringName())
        animationTree.set("parameters/conditions/isStretchingUp".asStringName(), true)
        animationTree.set("parameters/conditions/isStretchingDown".asStringName(), false)

        // 1.512 - 1.190 = 0.322
        positionMutate { z += HEIGHT_DELTA }
    }

    @RegisterFunction
    fun stretchDown() {

        if (!isStretched) return
        if (!resetStretchTimer.isStopped()) resetStretchTimer.stop()

        animationTree.set("parameters/conditions/isStretchingUp".asStringName(), false)
        animationTree.set("parameters/conditions/isStretchingDown".asStringName(), true)
        isStretched = false
        positionMutate { z -= HEIGHT_DELTA }
    }

    private fun updateHorizontalPosition(movePoint: HorizontalMovePoint, reset: Boolean = false) {

        val movePointNode = horizontalMovePoints[movePoint.ordinal].also {
            currentHorizontalPoint = movePoint
        }

        // Did Kodee actually move from its position?
        if (reset) {
            // debug("[info] :: Reset timer position")
            swooshSound.play()
            resetPositionTimer.start(resetHorizontalPositionTime)
        }
        if (reset && resetPositionTimer.timeLeft > 0) {
            swooshSound.play()
            resetPositionTimer.stop()
            resetPositionTimer.start(resetHorizontalPositionTime)
        }

        createTween()?.apply {
            tweenProperty(this@Kodee, "position:x".asNodePath(), movePointNode.position.x, 0.25)
                ?.setEase(Tween.EaseType.EASE_IN)
                ?.setTrans(Tween.TransitionType.TRANS_LINEAR)
                ?.from(position.x)
        }
    }

    @RegisterFunction
    fun onAreaExit(area3D: Area3D) {
        // We deregister only skip area from the `collidingSkipArea`s
//        debug(area3D.toString())
        if (!area3D.isInGroup(MailBox.SKIP_AREA_GROUP_NAME.asStringName())) return

        // Remove skip area from the set
        skipCollidingAreas.remove(area3D)

        // Did Kodee touch at least one mailbox?
        if (skipCollidingAreas.isEmpty() && !mailboxTouched) {
            comboLost.emit()
            decelerate()
        }

        if (skipCollidingAreas.isEmpty() && mailboxTouched) {
            mailboxTouched = false
        }
    }

    @RegisterFunction
    fun onAreaEnter(area3D: Area3D) {
        when {
            area3D.isInGroup(MailBox.SKIP_AREA_GROUP_NAME.asStringName()) -> {
                skipCollidingAreas.add(area3D)
            }

            area3D.isInGroup(MailBox.GROUP_NAME.asStringName()) -> {
                mailboxTouched = true
                comboChanged.emit(1)
            }
        }
    }

    @RegisterFunction
    fun onResetPositionTimeout() {
        updateHorizontalPosition(HorizontalMovePoint.CENTER, reset = false)
    }

    @RegisterFunction
    fun onDistanceWithDogChanged(distance: Int) {
        animationTree.onDistanceWithDogChanged(distance)
    }
}