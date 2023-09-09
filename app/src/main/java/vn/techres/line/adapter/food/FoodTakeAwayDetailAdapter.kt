package vn.techres.line.adapter.food

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import vn.techres.line.R
import vn.techres.line.data.model.food.FoodTakeAway
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemFoodTakeAwayDetailBinding
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.utils.CommonUtils.Companion.dpToPx
import vn.techres.line.interfaces.food.FoodTakeAwayDetailHandler
import java.text.DecimalFormat
import java.util.*
import java.util.stream.Collectors

class FoodTakeAwayDetailAdapter(var context: Context) :
    RecyclerView.Adapter<FoodTakeAwayDetailAdapter.FoodHolder>() {
    private var foodList = ArrayList<FoodTakeAway>()
    private var foodListTemp = ArrayList<FoodTakeAway>()
    private var foodTakeAwayDetailHandler : FoodTakeAwayDetailHandler? = null
    private val configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(foodList: ArrayList<FoodTakeAway>) {
        this.foodList = foodList
        this.foodListTemp = foodList
        notifyDataSetChanged()
    }

    fun setFoodTakeAwayDetailHandler(foodTakeAwayDetailHandler : FoodTakeAwayDetailHandler){
        this.foodTakeAwayDetailHandler = foodTakeAwayDetailHandler
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault()).trim()
        foodList = if (StringUtils.isNullOrEmpty(key)) {
            foodListTemp
        } else {
            foodListTemp.stream().filter {
                (it.name ?: "").lowercase(Locale.ROOT)
                    .contains(key) || (it.price ?: 0).toString().lowercase(Locale.ROOT)
                    .contains(key) || (it.normalize_name ?: "").lowercase(Locale.ROOT)
                    .contains(key) || (it.prefix ?: "").lowercase(Locale.ROOT)
                    .contains(key)
            }.collect(Collectors.toList()) as ArrayList<FoodTakeAway>
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodHolder {
        return FoodHolder(
            ItemFoodTakeAwayDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FoodHolder, position: Int) {
        holder.bind(context, configNodeJs, foodList[position], foodTakeAwayDetailHandler)
    }

    override fun getItemCount(): Int = foodList.size

    class FoodHolder(private val binding: ItemFoodTakeAwayDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, configNodeJs: ConfigNodeJs, food: FoodTakeAway, foodTakeAwayDetailHandler : FoodTakeAwayDetailHandler?) {
            Glide.with(binding.imgAvatarFood)
                .load(String.format("%s%s", configNodeJs.api_ads, food.avatar?.medium))
                .transform(CenterCrop(), RoundedCorners(dpToPx(10)))
                .into(binding.imgAvatarFood)
            binding.tvNameFood.text = food.name ?: ""
            binding.tvPriceFood.text = String.format("%s%s",
                Currency.formatCurrencyDecimal(food.price ?: 0F),
                context.resources.getString(R.string.denominations)
            )
            binding.tvRatingFood.text = DecimalFormat("0.0").format(food.branchStar ?: 0F)
            binding.root.setOnClickListener {
                foodTakeAwayDetailHandler?.onChooseFood(food)
            }

        }
    }

}