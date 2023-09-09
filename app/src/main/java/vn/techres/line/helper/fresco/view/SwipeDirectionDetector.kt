package vn.techres.line.helper.fresco.view

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.atan2
import kotlin.math.sqrt

abstract class SwipeDirectionDetector() {

    abstract fun onDirectionDetected(direction: Direction?)

    private var touchSlop = 0
    private var startX = 0f
    private var startY = 0f
    private var isDetected = false

    constructor(context: Context) : this() {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                startX = event.x
//                startY = event.y
//            }
//            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
//                if (!isDetected) {
//                    onDirectionDetected(Direction.NOT_DETECTED)
//                }
//                run {
//                    startY = 0.0f
//                    startX = startY
//                }
//                isDetected = false
//            }
            MotionEvent.ACTION_MOVE -> if (!isDetected && getDistance(event) > touchSlop) {
                isDetected = true
                val x = event.x
                val y = event.y
                val direction = getDirection(startX, startY, x, y)
                onDirectionDetected(direction)
            }
        }
        return false
    }

    /**
     * Given two points in the plane p1=(x1, x2) and p2=(y1, y1), this method
     * returns the direction that an arrow pointing from p1 to p2 would have.
     *
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the direction
     */
    private fun getDirection(x1: Float, y1: Float, x2: Float, y2: Float): Direction {
        val angle = getAngle(x1, y1, x2, y2)
        return Direction[angle]
    }

    /**
     * Finds the angle between two points in the plane (x1,y1) and (x2, y2)
     * The angle is measured with 0/360 being the X-axis to the right, angles
     * increase counter clockwise.
     *
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the angle between two points
     */
    private fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val rad = atan2((y1 - y2).toDouble(), (x2 - x1).toDouble()) + Math.PI
        return (rad * 180 / Math.PI + 180) % 360
    }

    private fun getDistance(ev: MotionEvent): Float {
        var distanceSum = 0f
        val dx = ev.getX(0) - startX
        val dy: Float = ev.getY(0) - startY
        distanceSum += sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        return distanceSum
    }

    enum class Direction {
        NOT_DETECTED, UP, DOWN, LEFT, RIGHT;

        companion object {
            operator fun get(angle: Double): Direction {
                return if (inRange(angle, 45f, 135f)) {
                    UP
                } else if (inRange(angle, 0f, 45f) || inRange(angle, 315f, 360f)) {
                    RIGHT
                } else if (inRange(angle, 225f, 315f)) {
                    DOWN
                } else {
                    LEFT
                }
            }

            private fun inRange(angle: Double, init: Float, end: Float): Boolean {
                return angle >= init && angle < end
            }
        }
    }
}