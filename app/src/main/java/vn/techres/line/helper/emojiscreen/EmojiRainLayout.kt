package vn.techres.line.helper.emojiscreen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.util.Pools
import androidx.percentlayout.widget.PercentFrameLayout
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import vn.techres.line.R
import java.util.concurrent.TimeUnit

class EmojiRainLayout : PercentFrameLayout {
    private var EMOJI_STANDARD_SIZE = 0

    private val RELATIVE_DROP_DURATION_OFFSET = 0.25f

    private val DEFAULT_PER = 6

    private val DEFAULT_DURATION = 8000

    private val DEFAULT_DROP_DURATION = 2400

    private val DEFAULT_DROP_FREQUENCY = 500

    private var mSubscriptions: CompositeSubscription? = null

    private var mWindowHeight = 0

    private var mEmojiPer = 0

    private var mDuration = 0

    private var mDropAverageDuration = 0

    private var mDropFrequency = 0

    private var mEmojis = ArrayList<Drawable>()
    private var iResEmoji = 0

    private var mEmojiPool: Pools.SynchronizedPool<ImageView>? = null

    init{
        EMOJI_STANDARD_SIZE = dip2px(36)

    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        if (context != null) {
            if (attrs != null) {
                init(context, attrs)
            }
        }
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        if (context != null) {
            if (attrs != null) {
                init(context, attrs)
            }
        }
    }

    fun setPer(per: Int) {
        mEmojiPer = per
    }

    fun setDuration(duration: Int) {
        mDuration = duration
    }

    fun setDropDuration(dropDuration: Int) {
        mDropAverageDuration = dropDuration
    }

    fun setDropFrequency(frequency: Int) {
        mDropFrequency = frequency
    }

    fun addEmoji(emoji: Bitmap?) {
        mEmojis.add(BitmapDrawable(resources, emoji))
    }

    fun addEmoji(emoji: Drawable?) {
        if (emoji != null) {
            mEmojis.add(emoji)
        }
    }

    fun addEmoji(@DrawableRes resId: Int) {
        iResEmoji = resId
        ContextCompat.getDrawable(context, resId)?.let { mEmojis.add(it) }
    }

    fun clearEmojis() {
        iResEmoji = 0
        if(mEmojis.size > 0){
            mEmojis = ArrayList()
        }
    }

    /**
     * Stop dropping animation after all emojis in the screen currently
     * dropping out of the screen.
     */
    fun stopDropping() {
        mSubscriptions?.clear()
    }

    /**
     * Start dropping animation.
     * The animation will last for n flow(s), which n is `mDuration`
     * divided by `mDropFrequency`.
     * The interval between two flows is `mDropFrequency`.
     * There will be `mEmojiPer` emojis dropping in each flow.
     * The dropping animation for a specific emoji is a random value with mean
     * `mDropAverageDuration` and relative offset `RELATIVE_DROP_DURATION_OFFSET`.
     */
    fun startDropping() {
        initEmojisPool()
        RandomsEmoji.setSeed(7)
        mWindowHeight = getWindowHeight()
        val subscription = Observable.interval(mDropFrequency.toLong(), TimeUnit.MILLISECONDS)
            .take(mDuration / mDropFrequency)
            .flatMap {
                Observable.range(
                    0,
                    mEmojiPer
                )
            }
            .map<ImageView?> { mEmojiPool!!.acquire() }
            .filter { e: ImageView? -> e != null }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { emoji: ImageView? -> startDropAnimationForSingleEmoji(emoji) }
            ) { obj: Throwable -> obj.printStackTrace() }
        mSubscriptions?.add(subscription)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.EmojiRainLayout)
        mSubscriptions = CompositeSubscription()
        mEmojis = ArrayList()
        mEmojiPer = ta.getInteger(R.styleable.EmojiRainLayout_per, DEFAULT_PER)
        mDuration = ta.getInteger(R.styleable.EmojiRainLayout_duration, DEFAULT_DURATION)
        mDropAverageDuration = ta.getInteger(
            R.styleable.EmojiRainLayout_dropDuration,
            DEFAULT_DROP_DURATION
        )
        mDropFrequency = ta.getInteger(
            R.styleable.EmojiRainLayout_dropFrequency,
            DEFAULT_DROP_FREQUENCY
        )
        ta.recycle()
    }

    private fun startDropAnimationForSingleEmoji(emoji: ImageView?) {
        val translateAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0F,
            Animation.RELATIVE_TO_SELF, RandomsEmoji.floatAround(0f, 5f),
            Animation.RELATIVE_TO_PARENT, 0F,
            Animation.ABSOLUTE, mWindowHeight.toFloat()
        )
        translateAnimation.duration = ((mDropAverageDuration * RandomsEmoji.floatAround(
            1f,
            RELATIVE_DROP_DURATION_OFFSET
        )).toLong())
        translateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                mEmojiPool?.release(emoji!!)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        emoji?.startAnimation(translateAnimation)
    }

    private fun getWindowHeight(): Int {
        val windowManager = context.applicationContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        return point.y
    }

    private fun initEmojisPool() {
//        val emojiTypeCount = mEmojis.size
        val emojiTypeCount = if(iResEmoji != 0){
            1
        }else{
            0
        }
        check(emojiTypeCount != 0) { "There are no emojis" }
        clearDirtyEmojisInPool()
        val expectedMaxEmojiCountInScreen = (((1 + RELATIVE_DROP_DURATION_OFFSET)
                * mEmojiPer
                * mDropAverageDuration)
                / mDropFrequency.toFloat()).toInt()
        mEmojiPool = Pools.SynchronizedPool(expectedMaxEmojiCountInScreen)
        for (i in 0 until expectedMaxEmojiCountInScreen) {
//            val emoji = generateEmoji(mEmojis[i % emojiTypeCount])
            val emoji = generateEmoji(ContextCompat.getDrawable(context, iResEmoji)!!)
            addView(emoji, 0)
            mEmojiPool?.release(emoji)
        }
    }

    private fun generateEmoji(emojiDrawable: Drawable): ImageView {
        val emoji = ImageView(context)
        emoji.setImageDrawable(emojiDrawable)
        val width =
            (EMOJI_STANDARD_SIZE * (1.0 + RandomsEmoji.positiveGaussian())).toInt()
        val height =
            (EMOJI_STANDARD_SIZE * (1.0 + RandomsEmoji.positiveGaussian())).toInt()
        val params = LayoutParams(width, height)
        params.percentLayoutInfo.leftMarginPercent = RandomsEmoji.floatStandard()
        params.topMargin = -height
        params.leftMargin = (-0.5f * width).toInt()
        emoji.layoutParams = params
        emoji.elevation = 100f
        return emoji
    }

    private fun clearDirtyEmojisInPool() {
        if (mEmojiPool != null) {
            var dirtyEmoji: ImageView?
            while (mEmojiPool?.acquire().also { dirtyEmoji = it } != null) removeView(dirtyEmoji)
        }
    }

    private fun dip2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}