package vn.techres.line.adapter.newsfeed

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.data.line.model.PostReview
import vn.techres.line.databinding.ItemSearchNewsFeedBinding
import vn.techres.line.interfaces.newsfeed.SearchNewsFeedHandler

class SearchNewsFeedAdapter(var context: Context) :
    RecyclerView.Adapter<SearchNewsFeedAdapter.ItemHolder>() {
    private var dataSource = ArrayList<PostReview>()
    private var searchNewsFeedHandler: SearchNewsFeedHandler? = null

    fun setDataSource(dataSource: ArrayList<PostReview>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickItemSearchNewsFeed(searchNewsFeedHandler: SearchNewsFeedHandler) {
        this.searchNewsFeedHandler = searchNewsFeedHandler
    }

    inner class ItemHolder(val binding: ItemSearchNewsFeedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemSearchNewsFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val data = dataSource[position]
            if (data.title!!.isEmpty()) {
                binding.txtTitleSearch.text =
                    "Bài viết không có tiêu đề của " + data.customer.full_name
            } else {
                binding.txtTitleSearch.text = data.title
            }

            binding.txtTitleSearch.setOnClickListener {
                searchNewsFeedHandler!!.onClickItem(data)
            }

        }

    }

}