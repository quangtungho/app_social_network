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
import vn.techres.line.databinding.ItemFoodTakeAwayHomeBinding
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.utils.CommonUtils.Companion.dpToPx
import vn.techres.line.helper.utils.setMargins
import vn.techres.line.interfaces.food.FoodHomeHandler

class FoodTakeAwayHomeAdapter(var context : Context) : RecyclerView.Adapter<FoodTakeAwayHomeAdapter.FoodHolder>(){
    private var foodList = ArrayList<FoodTakeAway>()
    private var foodHomeHandler : FoodHomeHandler? = null
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(foodList : ArrayList<FoodTakeAway>){
        this.foodList = foodList
        notifyDataSetChanged()
    }
    fun setFoodHomeHandler(foodHomeHandler : FoodHomeHandler?){
        this.foodHomeHandler = foodHomeHandler
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodHolder {
        return FoodHolder(ItemFoodTakeAwayHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FoodHolder, position: Int) {
        holder.bind(configNodeJs, foodList[position])
    }

    override fun getItemCount(): Int = foodList.size

    inner class FoodHolder(private val binding : ItemFoodTakeAwayHomeBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(configNodeJs: ConfigNodeJs, food : FoodTakeAway? = null){
            if(itemCount == bindingAdapterPosition){
                binding.root.setMargins(
                    dpToPx(16),0, dpToPx(16),0
                )
            }
            Glide.with(binding.imgAvatarFood)
                .load(String.format("%s%s", configNodeJs.api_ads, food?.avatar?.medium))
                .placeholder(R.drawable.food_demo)
                .transform(CenterCrop(), RoundedCorners(dpToPx(12)))
                .into(binding.imgAvatarFood)
            binding.tvNameFood.text = food?.name
            binding.tvTotalRating.text = "5.0"
            binding.tvPriceFood.text = String.format("%s%s",
                Currency.formatCurrencyDecimal(food?.price ?: 0F),
                context.resources.getString(R.string.denominations)
            )
        }
    }
}