package vn.techres.line.adapter.food

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.R
import vn.techres.line.data.model.food.FoodPurcharePoint
import vn.techres.line.data.model.utils.CartPointFoodTakeAway
import vn.techres.line.databinding.ItemFoodPointBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.FoodsPointHandler
import vn.techres.line.interfaces.OnLickItem

@SuppressLint("UseRequireInsteadOfGet")
class ChooseRewardPointAdapter(val context: Context) :
    RecyclerView.Adapter<ChooseRewardPointAdapter.ItemHolder>() {
    private var foodsPointHandler: FoodsPointHandler? = null
    private var dataFoodPoint = ArrayList<FoodPurcharePoint>()
    private var onClickItem: OnLickItem? = null
    private var foodPointList = ArrayList<FoodPurcharePoint>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun onFoodsPointHandler(foodsPointHandler: FoodsPointHandler) {
        this.foodsPointHandler = foodsPointHandler
    }

    fun setDataSource(dataFoodPoint: ArrayList<FoodPurcharePoint>) {
        this.dataFoodPoint = dataFoodPoint
        notifyDataSetChanged()
    }

    fun setOnClickItem(clickItem: OnLickItem) {
        this.onClickItem = clickItem
    }

    override fun getItemCount(): Int {
        return dataFoodPoint.size
    }

    inner class ItemHolder(val binding: ItemFoodPointBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemFoodPointBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val food = dataFoodPoint[position]

            binding.imgLogo.load(String.format("%s%s", nodeJs.api_ads, food.food_image?.original)) {
                transformations(RoundedCornersTransformation(10F))
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
            }

            binding.txtNameFood.text = food.name
            binding.txtPriceFood.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(food.price!!.toFloat()),
                context.resources.getString(R.string.denominations)
            )

//        binding.txtDes.text = dataFoodPoint[position].description
            binding.txtPoint.text = String.format(
                "%s %s",
                food.point_to_purchase.toString(),
                context.resources.getString(R.string.point)
            )

            binding.imgLogo.setOnClickListener {
                onClickItem!!.lickPosition(position)
            }

            binding.txtQuantity.text = ""

            try {
                val jsonCartPoint =
                    AppConfig.cacheManager.get(TechresEnum.KEY_CART_POINT_CHOOSE.toString())
                if (!jsonCartPoint.isNullOrBlank()) {
                    val cartPointCheck =
                        Gson().fromJson<CartPointFoodTakeAway>(jsonCartPoint, object :
                            TypeToken<CartPointFoodTakeAway>() {}.type)
                    foodPointList = cartPointCheck.food
                    for (i in foodPointList.indices) {
                        if (foodPointList[i].id == food.id) {
                            binding.txtQuantity.visibility = View.VISIBLE
                            binding.txtQuantity.text =
                                String.format("%s%s", "x", foodPointList[i].quantity.toString())
                        }
                        if (foodPointList[i].quantity == 0 && foodPointList[i].id == food.id) {
                            binding.txtQuantity.visibility = View.GONE
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            itemView.setOnClickListener {
                foodPointList.let {
                    if (foodPointList.any { it.id == food.id }) {
                        foodsPointHandler!!.onClickShow(
                            position,
                            foodPointList[foodPointList.indexOfFirst { it.id == food.id }]
                        )
                    } else {
                        foodsPointHandler!!.onClickShow(position, food)
                    }
                }
            }
        }

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

}
