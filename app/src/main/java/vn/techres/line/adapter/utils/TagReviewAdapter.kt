package vn.techres.line.adapter.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemTagReviewBinding
import vn.techres.line.interfaces.TagReviewHandler

class TagReviewAdapter(val context: Context) : RecyclerView.Adapter<TagReviewAdapter.ItemHolder>() {
    private var listTagReview = ArrayList<String>()
    private var tagReviewHandler: TagReviewHandler? = null

    fun setDataSource(listTagReview: ArrayList<String>) {
        this.listTagReview = listTagReview
    }

    fun setTagReviewHandler(tagReviewHandler: TagReviewHandler) {
        this.tagReviewHandler = tagReviewHandler
    }

    inner class ItemHolder(val binding: ItemTagReviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemTagReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return listTagReview.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            binding.txtTagReview.text = listTagReview[position]
            binding.txtTagReview.setOnClickListener {
                tagReviewHandler!!.onClick(listTagReview[position])
            }
        }

    }

}