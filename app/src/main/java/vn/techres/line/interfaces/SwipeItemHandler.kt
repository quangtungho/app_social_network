package vn.techres.line.interfaces

import androidx.recyclerview.widget.RecyclerView

interface SwipeItemHandler {
    /**
     * Called as a view holder is actively being swiped/rebounded.
     *
     * @param currentSwipePercentage The total percentage the view has been swiped.
     * @param swipeThreshold The percentage needed to consider a swipe as "rebounded"
     *  or "swiped"
     * @param currentTargetHasMetThresholdOnce Whether or not during a contiguous interaction
     *  with a single view holder, the swipe percentage has ever been greater than the swipe
     *  threshold.
     */
    fun onReboundOffsetChanged(
        currentSwipePercentage: Float,
        swipeThreshold: Float,
        currentTargetHasMetThresholdOnce: Boolean,
        actionState: Int,
        viewHolder: RecyclerView.ViewHolder
    )
    /**
     * Called once all interaction (user initiated swiping and animations) has ended and this
     * view holder has been swiped passed the swipe threshold.
     */
    fun onRebounded(viewHolder: RecyclerView.ViewHolder)

    fun onVibrator(position : Int)

}