package vn.techres.line.adapter.food

import android.annotation.SuppressLint
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
import vn.techres.line.helper.StringUtils
import vn.techres.line.interfaces.ChooseMenuFoodHandler
import java.util.*
import java.util.stream.Collectors

@SuppressLint("UseRequireInsteadOfGet")
class ChooseMenuFoodAdapter(val context: Context) :
    RecyclerView.Adapter<ChooseMenuFoodAdapter.ItemHolder>() {
    private var dataSource = ArrayList<FoodMenu>()
    private var dataSourceTemp = ArrayList<FoodMenu>()
    private var chooseMenuFoodHandler: ChooseMenuFoodHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(dataSource: ArrayList<FoodMenu>) {
        this.dataSource = dataSource
        this.dataSourceTemp = dataSource
        notifyDataSetChanged()
    }

    fun setChooseMenuFoodHandler(chooseMenuFoodHandler: ChooseMenuFoodHandler) {
        this.chooseMenuFoodHandler = chooseMenuFoodHandler
    }

    fun getDataSource(): ArrayList<FoodMenu> {
        return dataSource
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    // Inflates the item views
    inner class ItemHolder(val binding: ItemMenuFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemMenuFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            val food = dataSource[position]

            binding.imgLogo.load(String.format("%s%s", nodeJs.api_ads, food.avatar?.original)) {
                crossfade(true)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                transformations(RoundedCornersTransformation(10F))
            }

            binding.tvNameFood.text = food.food_name

            binding.tvPriceFood.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(food.price!!.toFloat()),
                context.resources.getString(R.string.denominations)
            )

            binding.tvQuantityFood.text = food.quantity.toString()

            binding.lnContainerFood.setOnClickListener {
                chooseMenuFoodHandler!!.onPlusQuality(food)
            }

            binding.imgLogo.setOnClickListener {
                chooseMenuFoodHandler!!.onRemoveFood(food)
            }

            binding.imgMinus.setOnClickListener {
                chooseMenuFoodHandler!!.onMinusQuality(food)
            }

            binding.imgPlus.setOnClickListener {
                chooseMenuFoodHandler!!.onPlusQuality(food)
            }
        }

    }

    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault()).trim()
        dataSource = if (StringUtils.isNullOrEmpty(key)) {
            dataSourceTemp
        } else {
            dataSourceTemp.stream().filter {
                it.food_name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.prefix!!.lowercase(Locale.ROOT)
                    .contains(key) || it.normalize_name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.price!!.toString().lowercase(Locale.ROOT)
                    .contains(key)
            }.collect(Collectors.toList()) as ArrayList<FoodMenu>
        }
        notifyDataSetChanged()
    }

}


