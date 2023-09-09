package vn.techres.line.adapter.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.item_spinner_feedback_one.view.*
import vn.techres.line.R
import vn.techres.line.data.model.ItemSpinner


class SpinnerAdapter(ctx: Context,
                     moods: List<ItemSpinner>) :
    ArrayAdapter<ItemSpinner>(ctx, 0, moods) {
    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }
    override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }
    private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val mood = getItem(position)
        val view = recycledView ?: LayoutInflater.from(context).inflate(
            R.layout.item_spinner_feedback_one,
            parent,
            false
        )
        view.ic_img_view.setImageResource(mood!!.image)
        view.tv_ic_img_view.text = mood.description
        return view
    }
}