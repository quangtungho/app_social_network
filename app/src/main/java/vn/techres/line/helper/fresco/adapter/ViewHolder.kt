package vn.techres.line.helper.fresco.adapter

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup

abstract class ViewHolder() {
    private val STATE: String = ViewHolder::class.java.simpleName
    var itemView: View? = null
    var mIsAttached = false
    var mPosition = 0

    private enum class VolumeState {
        ON, OFF
    }

    constructor(itemView: View?) : this(){
        requireNotNull(itemView) { "itemView should not be null" }
        this.itemView = itemView

    }

    open fun attach(parent: ViewGroup, position: Int) {
        mIsAttached = true
        mPosition = position
        parent.addView(itemView)
    }

    open fun detach(parent: ViewGroup) {
        parent.removeView(itemView)
        mIsAttached = false
    }

    open fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val ss = if (state.containsKey(STATE)) state.getSparseParcelableArray<Parcelable>(STATE) else null
            if (ss != null) {
                itemView!!.restoreHierarchyState(ss)
            }
        }
    }

    open fun onSaveInstanceState(): Parcelable? {
        val state = SparseArray<Parcelable>()
        itemView!!.saveHierarchyState(state)
        val bundle = Bundle()
        bundle.putSparseParcelableArray(STATE, state)
        return bundle
    }


}