package vn.techres.line.helper.fresco.adapter

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import java.util.*

abstract class RecyclingPagerAdapter<VH : ViewHolder?> : PagerAdapter() {
    abstract val itemCount: Int

    abstract fun onBindViewHolder(holder: VH, position: Int)

    abstract fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH
    abstract fun getItemViewType(position: Int) : Int

    private val STATE = RecyclingPagerAdapter::class.java.simpleName
    private val TAG = RecyclingPagerAdapter::class.java.simpleName
    private var DEBUG = false

    private val mRecycleTypeCaches = SparseArray<RecycleCache?>()

    private var mSavedStates = SparseArray<Parcelable>()


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (`object` is ViewHolder) {
            `object`.detach(container)
        }
    }

    override fun getCount(): Int {
        return itemCount
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): ViewHolder {
        val viewType = getItemViewType(position)
        if (mRecycleTypeCaches[viewType] == null) {
            mRecycleTypeCaches.put(viewType, RecycleCache(this))
        }
        val viewHolder: ViewHolder = mRecycleTypeCaches[viewType]!!.getFreeViewHolder(
            container,
            viewType
        )
        viewHolder.attach(container, position)
        onBindViewHolder(viewHolder as VH, position)
        viewHolder.onRestoreInstanceState(mSavedStates[getItemId(position)])
        return viewHolder
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return `object` is ViewHolder && `object`.itemView === view
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        for (viewHolder in attachedViewHolders) {
            onNotifyItemChanged()
        }
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        if (state is Bundle) {
            state.classLoader = loader
            val ss = if (state.containsKey(STATE)) state.getSparseParcelableArray<Parcelable>(STATE) else null
            mSavedStates = ss ?: SparseArray()
        }
        super.restoreState(state, loader)
    }

    override fun saveState(): Parcelable {
        val bundle = Bundle()
        for (viewHolder in attachedViewHolders) {
            mSavedStates.put(
                getItemId((viewHolder as VH)!!.mPosition),
                viewHolder.onSaveInstanceState()
            )
        }
        bundle.putSparseParcelableArray(STATE, mSavedStates)
        return bundle
    }

    fun getItemId(position: Int): Int {
        return position
    }


    private fun onNotifyItemChanged() {}

    private val attachedViewHolders: List<Any>
        get() {
            val attachedViewHolders: MutableList<ViewHolder> = ArrayList<ViewHolder>()
            val n = mRecycleTypeCaches.size()
            for (i in 0 until n) {
                for (viewHolder in mRecycleTypeCaches[mRecycleTypeCaches.keyAt(i)]!!.mCaches!!) {
                    if (viewHolder.mIsAttached) {
                        attachedViewHolders.add(viewHolder)
                    }
                }
            }
            return attachedViewHolders
        }

    class RecycleCache() {
        var mCaches: MutableList<ViewHolder>? = null
        var mAdapter: RecyclingPagerAdapter<*>? = null

        constructor(mAdapter: RecyclingPagerAdapter<*>) : this(){
            this.mAdapter = mAdapter
            mCaches = ArrayList()
        }

        fun getFreeViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            var i = 0
            var viewHolder: ViewHolder
            val n = mCaches!!.size
            while (i < n) {
                viewHolder = mCaches!![i]
                if (!viewHolder.mIsAttached) {
                    return viewHolder
                }
                i++
            }
            viewHolder = mAdapter!!.onCreateViewHolder(parent, viewType)!!
            mCaches!!.add(viewHolder)
            return viewHolder
        }
    }

}