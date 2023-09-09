package vn.techres.line.adapter.restaurant

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.databinding.ItemRestaurantCardBinding
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.interfaces.LickItem
import java.util.*
import java.util.stream.Collectors

class RestaurantCardAdapter(var context: Context) :
    RecyclerView.Adapter<RestaurantCardAdapter.ItemHolder>() {
    private var lickItemHandler: LickItem? = null
    private var dataSource = ArrayList<RestaurantCard>()
    private var dataSourceTemp = ArrayList<RestaurantCard>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setLickItem(lickItem: LickItem) {
        this.lickItemHandler = lickItem
    }

    fun setDataSource(dataSource: ArrayList<RestaurantCard>) {
        this.dataSource = dataSource
        this.dataSourceTemp = dataSource
        notifyDataSetChanged()
    }


    inner class ItemHolder(val binding: ItemRestaurantCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemRestaurantCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault())
        dataSource = if (StringUtils.isNullOrEmpty(key)) {
            dataSourceTemp
        } else {
            dataSourceTemp.stream().filter {
                it.normalize_name!!.lowercase(Locale.ROOT)
                    .contains(keyword) || it.normalize_name!!.lowercase(Locale.ROOT)
                    .contains(keyword) || it.restaurant_name!!.lowercase(Locale.ROOT)
                    .contains(keyword)
            }.collect(Collectors.toList()) as ArrayList<RestaurantCard>
        }
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val item = dataSource[position]
            binding.imgAvatarBranch.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    item.restaurant_avatar_three_image?.medium
                )
            )
            {
                crossfade(true)
                scale(Scale.FILL)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                size(500, 500)
            }
//        holder.imgBackgroundCard.setBackgroundColor(myColor)
            binding.tvContentRestaurantCard.text = item.restaurant_info
            binding.tvNameRestaurantCard.text = item.restaurant_name
            binding.tvReviewedRestaurantCard.text = String.format("%.1f", item.average_rate!!)
            binding.tvBranchRestaurantCard.text = item.branch_count.toString()
            if (item.restaurant_address!!.isNotEmpty()) {
                binding.tvAddressRestaurantCard.text = String.format(
                    "%s: %s",
                    context.resources.getString(R.string.title_address),
                    item.restaurant_address
                )
            } else {
                binding.tvAddressRestaurantCard.text = String.format(
                    "%s: %s",
                    context.resources.getString(R.string.title_address),
                    context.resources.getString(R.string.updating)
                )
            }

            binding.tvTypeRestaurantCard.text = item.restaurant_membership_card_name
            binding.tvPointRestaurantCard.text =
                Currency.formatCurrencyDecimal(item.total_all_point!!.toFloat())


            itemView.setOnClickListener {
                lickItemHandler!!.onClickItem(item)
            }
        }
    }

}