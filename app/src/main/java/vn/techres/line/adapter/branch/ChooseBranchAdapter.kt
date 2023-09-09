package vn.techres.line.adapter.branch

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import vn.techres.line.R
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.databinding.ItemResearchBranchBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.interfaces.BranchHandler
import java.util.*
import java.util.stream.Collectors

class ChooseBranchAdapter(val context: Context) :
    RecyclerView.Adapter<ChooseBranchAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Branch>()
    private var dataSourceTemp = ArrayList<Branch>()
    private var chooseBranchHandler: BranchHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<Branch>) {
        this.dataSource = dataSource
        this.dataSourceTemp = dataSource
        notifyDataSetChanged()
    }

    fun setChooseRestaurant(chooseBranchHandler: BranchHandler) {
        this.chooseBranchHandler = chooseBranchHandler
    }

    inner class ItemHolder(val binding: ItemResearchBranchBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemResearchBranchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder)
        {
            val branch = dataSource[position]
            Glide.with(binding.imgAvatarBranch)
                .load(String.format("%s%s", nodeJs.api_ads, branch.image_logo_url.original))
                .transform(CenterInside(), RoundedCorners(10))
                .placeholder(R.drawable.logo_aloline_placeholder)
                .error(R.drawable.logo_aloline_placeholder)
                .into(binding.imgAvatarBranch)
            binding.txtNameRestaurant.text = String.format(
                "%s: %s",
                context.getString(R.string.belong_to_restaurant),
                branch.restaurant_name
            )
            binding.txtNameBranch.text = branch.name
            binding.txtAddressBranch.text = branch.address_full_text
            binding.txtRatingBranch.text = String.format("%.1f", branch.rating ?: 0F)

            itemView.setOnClickListener {
                chooseBranchHandler?.onChooseBranch(branch)
            }
        }

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault()).trim()
        dataSource = if (StringUtils.isNullOrEmpty(key)) {
            dataSourceTemp
        } else {
            dataSourceTemp.stream().filter {
                it.restaurant_name!!.lowercase(Locale.getDefault())
                    .contains(keyword) || it.name!!.lowercase(Locale.getDefault())
                    .contains(keyword)
            }.collect(Collectors.toList()) as ArrayList<Branch>
        }
        notifyDataSetChanged()
    }

}