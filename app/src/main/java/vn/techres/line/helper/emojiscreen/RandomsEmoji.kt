package vn.techres.line.helper.emojiscreen

import java.util.*

object RandomsEmoji {
    private val random = Random()

    fun setSeed(seed: Long) {
        random.setSeed(seed)
    }

    fun floatStandard(): Float {
        return random.nextFloat()
    }

    fun floatAround(mean: Float, delta: Float): Float {
        return floatInRange(mean - delta, mean + delta)
    }

    fun floatInRange(left: Float, right: Float): Float {
        return left + (right - left) * random.nextFloat()
    }

    fun positiveGaussian(): Double {
        return Math.abs(random.nextGaussian())
    }
}