package vn.techres.line.adapter.booking

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import vn.techres.line.R
import vn.techres.line.data.model.booking.FoodMenu
import vn.techres.line.databinding.ItemMenuFoodBinding
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.ChooseMenuFoodHandler

class CartFoodAdapter(var context: Context) : RecyclerView.Adapter<CartFoodAdapter.ItemHolder>() {

    private var foods = ArrayList<FoodMenu>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var chooseMenuFoodHandler: ChooseMenuFoodHandler? = null

    fun setChooseMenuFoodHandler(chooseMenuFoodHandler: ChooseMenuFoodHandler) {
        this.chooseMenuFoodHandler = chooseMenuFoodHandler
    }

    fun setDataSource(dataSource: ArrayList<FoodMenu>){
        this.foods = dataSource
        notifyDataSetChanged()
    }


    inner class ItemHolder(val binding: ItemMenuFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemMenuFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            val food = foods[position]

            binding.imgLogo.load(String.format("%s%s", nodeJs.api_ads, food.avatar?.original)){
                crossfade(true)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                transformations(RoundedCornersTransformation(10F))
            }

            binding.tvNameFood.text = food.food_name

            binding.tvPriceFood.text = String.format("%s %s", Currency.formatCurrencyDecimal(food.price!!.toFloat()), context.resources.getString(
                R.string.denominations))

            binding.tvQuantityFood.text = food.quantity.toString()

            binding.lnContainerFood.setOnClickListener {
                chooseMenuFoodHandler!!.onPlusQualityCart(position)
            }

            binding.imgLogo.setOnClickListener {
                chooseMenuFoodHandler!!.onRemoveFoodCart(position)
            }

            binding.imgMinus.setOnClickListener {
                chooseMenuFoodHandler!!.onMinusQualityCart(position)
            }

            binding.imgPlus.setOnClickListener {
                chooseMenuFoodHandler!!.onPlusQualityCart(position)
            }

        }

    }

    override fun getItemCount(): Int {
        return foods.size
    }

}