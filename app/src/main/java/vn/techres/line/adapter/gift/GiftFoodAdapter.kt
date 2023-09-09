package vn.techres.line.adapter.gift

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.Food
import vn.techres.line.databinding.ItemFoodDetailGiftBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils

/**
 * @Author: Phạm Văn Nhân
 * @Date: 12/05/2022
 */
class GiftFoodAdapter(val context: Context) :
    RecyclerView.Adapter<GiftFoodAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Food>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<Food>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemFoodDetailGiftBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemFoodDetailGiftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val data = dataSource[position]
            Utils.showImage(
                binding.imgFoodAvatar,
                String.format("%s%s", nodeJs.api_ads, data.avatar)
            )
            binding.txtFoodName.text = data.name
            binding.txtFoodDescription.text = data.description
        }

    }

}