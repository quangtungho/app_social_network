package vn.techres.line.adapter.food

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemSuggestFoodDetailBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.food.FoodDetailHandler

class SuggestFoodDetailAdapter(var context: Context) :
    RecyclerView.Adapter<SuggestFoodDetailAdapter.FoodHolder>() {
    private var foodDetailHandler : FoodDetailHandler? = null
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setFoodDetailHandler(foodDetailHandler : FoodDetailHandler){
        this.foodDetailHandler = foodDetailHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodHolder {
        return FoodHolder(
            ItemSuggestFoodDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FoodHolder, position: Int) {
        holder.bind(foodDetailHandler)
    }

    override fun getItemCount(): Int = 5

    class FoodHolder(var binding: ItemSuggestFoodDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(foodDetailHandler : FoodDetailHandler?) {
            binding.root.setOnClickListener {
//                foodDetailHandler?.onChooseFoodSuggest()
            }
        }
    }
}