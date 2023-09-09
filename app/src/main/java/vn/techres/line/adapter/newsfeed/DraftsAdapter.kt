package vn.techres.line.adapter.newsfeed

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.databinding.ItemDraftsBinding

/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/9/2022
 */
class DraftsAdapter(var context: Context) : RecyclerView.Adapter<DraftsAdapter.ItemHolder>() {
    private var dataSource = ArrayList<PostReview>()

    fun setDataSource(dataSource: ArrayList<PostReview>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }


    inner class ItemHolder(val binding: ItemDraftsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemDraftsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val data = dataSource[position]
            if (data.title!!.trim() == "") {
                binding.txtTitle.text = context.resources.getString(R.string.not_yet_title)
            } else {
                binding.txtTitle.text = data.title
            }

            if (data.content!!.trim() == "") {
                binding.txtContent.text = context.resources.getString(R.string.not_yet_content)
            } else {
                binding.txtContent.text = data.content
            }
        }


    }

}