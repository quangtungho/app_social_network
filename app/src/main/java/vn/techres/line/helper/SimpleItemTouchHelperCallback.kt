package vn.techres.line.helper

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.holder.message.*
import vn.techres.line.holder.message.left.RevokeMessageLeftHolder
import vn.techres.line.holder.message.left.VideoCallMessageLeftHolder
import vn.techres.line.holder.message.right.RevokeMessageRightHolder
import vn.techres.line.holder.message.right.VideoCallMessageRightHolder
import vn.techres.line.interfaces.SwipeItemHandler
import kotlin.math.abs
import kotlin.math.ln

class SimpleItemTouchHelperCallback(private var swipeItemHandler: SwipeItemHandler) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    private var currentTargetHasMetThresholdOnce = false
    private var currentTargetPosition: Int = -1

    companion object {
        private const val trueSwipeThreshold = 0.8F
        private const val swipeReboundingElasticity = 0.8F

    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = Float.MAX_VALUE


    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return Float.MAX_VALUE
    }


    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return Float.MAX_VALUE
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return when (viewHolder) {
            is RevokeMessageLeftHolder, is RevokeMessageRightHolder, is PinnedMessageHolder, is UtilsMessageHolder, is CreateGroupPersonalMessageHolder,
            is CreateGroupMessageHolder, is MemberMessageHolder, is AddMemberMessageHolder, is NotificationMessageHolder, is VideoCallMessageLeftHolder,
            is VideoCallMessageRightHolder-> {
                0
            }
            else -> {
                super.getSwipeDirs(recyclerView, viewHolder)
            }
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Do nothing. Overriding getSwipeThreshold to an impossible number means this will
        // never be called.
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (currentTargetHasMetThresholdOnce) {
            currentTargetHasMetThresholdOnce = false
            swipeItemHandler.onRebounded(viewHolder)
        }
        super.clearView(recyclerView, viewHolder)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (currentTargetPosition != viewHolder.bindingAdapterPosition) {
            currentTargetPosition = viewHolder.bindingAdapterPosition
            currentTargetHasMetThresholdOnce = false
        }
        val itemView = viewHolder.itemView
//        val currentSwipePercentage = (abs(dX) / itemView.width) * 2
        val currentSwipePercentage = abs(dX) - 50
        translateReboundingView(itemView, viewHolder, dX)
        if (currentSwipePercentage >= trueSwipeThreshold && !currentTargetHasMetThresholdOnce) {
            currentTargetHasMetThresholdOnce = true
            swipeItemHandler.onVibrator(viewHolder.bindingAdapterPosition)
        }
        getDefaultUIUtil().onDraw(
            c,
            recyclerView,
            itemView,
            dX / 2,
            dY,
            actionState,
            isCurrentlyActive
        )
//        var dXRelative: Float = dX / itemView.width * 3
//        // check size boundaries
//        // check size boundaries
//        if (dXRelative > 1) {
//            dXRelative = 1f
//        }
//        if (dXRelative < 0) {
//            dXRelative = 0f
//        }
//        when (viewHolder) {
//            is TextMessageRightHolder -> {
//                if (currentSwipePercentage >= trueSwipeThreshold && !currentTargetHasMetThresholdOnce) {
//                    viewHolder.binding.imgReplyQuick.animate().scaleX(dXRelative).scaleY(dXRelative)
//                        .setDuration(0).start()
//                    viewHolder.binding.imgReplyQuick.visibility = View.VISIBLE
//                    currentTargetHasMetThresholdOnce = true
//                    swipeItemHandler.onVibrator(viewHolder.bindingAdapterPosition)
//                }
//                getDefaultUIUtil().onDraw(
//                    c,
//                    recyclerView,
//                    viewHolder.binding.lnTextMessageRight,
//                    dX / 2,
//                    dY,
//                    actionState,
//                    isCurrentlyActive
//                )
//            }
//            is TextMessageLeftHolder -> {
//                if (currentSwipePercentage >= trueSwipeThreshold && !currentTargetHasMetThresholdOnce) {
//                    viewHolder.binding.imgReplyQuick.animate().scaleX(dXRelative).scaleY(dXRelative)
//                        .setDuration(0).start()
//                    viewHolder.binding.imgReplyQuick.visibility = View.VISIBLE
//                    currentTargetHasMetThresholdOnce = true
//                    swipeItemHandler.onVibrator(viewHolder.bindingAdapterPosition)
//                }
//                getDefaultUIUtil().onDraw(
//                    c,
//                    recyclerView,
//                    viewHolder.binding.lnTextMessageLeft,
//                    dX / 2,
//                    dY,
//                    actionState,
//                    isCurrentlyActive
//                )
//            }
//            else -> {
//                getDefaultUIUtil().onDraw(
//                    c,
//                    recyclerView,
//                    itemView,
//                    dX / 2,
//                    dY,
//                    actionState,
//                    isCurrentlyActive
//                )
//            }
//        }
    }

//    override fun onChildDrawOver(
//        c: Canvas,
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder?,
//        dX: Float,
//        dY: Float,
//        actionState: Int,
//        isCurrentlyActive: Boolean
//    ) {
//        when (viewHolder) {
//            is TextMessageRightHolder -> {
//                getDefaultUIUtil().onDraw(
//                    c,
//                    recyclerView,
//                    viewHolder.binding.imgReplyQuick,
//                    dX,
//                    dY,
//                    actionState,
//                    isCurrentlyActive
//                )
//            }
//            is TextMessageLeftHolder -> {
//                getDefaultUIUtil().onDraw(
//                    c,
//                    recyclerView,
//                    viewHolder.binding.imgReplyQuick,
//                    dX,
//                    dY,
//                    actionState,
//                    isCurrentlyActive
//                )
//            }
//            else -> {
//                getDefaultUIUtil().onDraw(
//                    c,
//                    recyclerView,
//                    viewHolder?.itemView,
//                    dX,
//                    dY,
//                    actionState,
//                    isCurrentlyActive
//                )
//            }
//        }
//
//    }

    private fun translateReboundingView(
        itemView: View,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float
    ) {

        val swipeDismissDistanceHorizontal = itemView.width * trueSwipeThreshold
        val dragFraction =
            ln((1 + (dX / swipeDismissDistanceHorizontal)).toDouble()) / ln(3.toDouble())
        val dragTo = dragFraction * swipeDismissDistanceHorizontal * swipeReboundingElasticity
        viewHolder.itemView.translationX = dragTo.toFloat()
    }
}