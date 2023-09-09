package vn.techres.line.adapter.gift

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.branch.BranchGift
import vn.techres.line.databinding.ItemGiftBranchBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
class GiftBranchAdapter(val context: Context) :
    RecyclerView.Adapter<GiftBranchAdapter.ItemHolder>() {
    private var dataSource = ArrayList<BranchGift>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<BranchGift>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemGiftBranchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemGiftBranchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            val data = dataSource[position]
            Utils.showImage(binding.imgLogo, String.format("%s%s", nodeJs.api_ads, data.image_logo_url))
            binding.txtBranchName.text = data.name
            binding.txtBranchAddress.text = data.address_full_text
        }

    }

}