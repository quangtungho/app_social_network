package vn.techres.line.adapter.newsfeed

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.fasterxml.jackson.databind.ObjectMapper
import vn.techres.line.R
import vn.techres.line.data.model.cart.CartFoodTakeAway
import vn.techres.line.data.model.food.FoodTakeAway
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemFoodsBranchBinding
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentCartFoodTakeAway
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.interfaces.DetailBranchHandler
import java.util.*
import java.util.stream.Collectors

@SuppressLint("UseRequireInsteadOfGet")
class FoodsTakeAwayAdapter(val context: Context) :
    RecyclerView.Adapter<FoodsTakeAwayAdapter.ViewHolder>() {
    private var dataSource = ArrayList<FoodTakeAway>()
    private var dataSourceTemp = ArrayList<FoodTakeAway>()
    private var detailBranch: DetailBranchHandler? = null
    private var branchId: Int? = null
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    val bundle = Bundle()
    val mapper = ObjectMapper()

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<FoodTakeAway>) {
        this.dataSource = dataSource
        this.dataSourceTemp = dataSource
        notifyDataSetChanged()
    }

    fun setBranchID(id: Int) {
        this.branchId = id
    }

    fun setOnClick(detailBranch: DetailBranchHandler) {
        this.detailBranch = detailBranch
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault()).trim()
        dataSource = if (StringUtils.isNullOrEmpty(key)) {
            dataSourceTemp
        } else {
            dataSourceTemp.stream().filter {
                (it.name ?: "").lowercase(Locale.ROOT)
                    .contains(key) || (it.prefix ?: "").lowercase(Locale.ROOT)
                    .contains(key) || (it.normalize_name ?: "").lowercase(Locale.ROOT)
                    .contains(key) || (it.price ?: 0F).toString().lowercase(Locale.ROOT)
                    .contains(key)
            }.collect(Collectors.toList()) as ArrayList<FoodTakeAway>
        }
        notifyDataSetChanged()
    }
    fun getDataSource() : ArrayList<FoodTakeAway>{
        return dataSource
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFoodsBranchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, configNodeJs, dataSource[position], detailBranch, position)
    }

    inner class ViewHolder(private val binding : ItemFoodsBranchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, configNodeJs: ConfigNodeJs, food : FoodTakeAway, detailBranch: DetailBranchHandler?, position: Int){
            Glide.with(binding.imgFood)
                .load(String.format("%s%s", configNodeJs.api_ads, food.avatar?.medium))
                .placeholder(R.drawable.food_demo)
                .transform(CenterCrop(), RoundedCorners(10))
                .into(binding.imgFood)
            binding.tvNameFood.text = food.name
            binding.tvPrice.text = String.format(
                "%s%s",
                Currency.formatCurrencyDecimal(food.price!!.toFloat()),
                context.resources.getString(R.string.denominations)
            )
            binding.lnQuantity.visibility = View.GONE
            binding.btnAdd.visibility = View.VISIBLE
            try {
                val cartCurrent = CurrentCartFoodTakeAway.getCurrentCartFoodTakeaway(context)
                if (branchId == cartCurrent.id_branch){
                    for (i in cartCurrent.food.indices){
                        if (cartCurrent.food[i].id == food.id){
                            binding.lnQuantity.visibility = View.VISIBLE
                            binding.btnAdd.visibility = View.GONE
                            binding.tvQuantity.text = cartCurrent.food[i].quantity.toString()
                        }
                    }
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            itemView.setOnClickListener {
                detailBranch!!.onDetailFood(food, position)
            }

            binding.imgIncrease.setOnClickListener {
                val cartCurrent = CurrentCartFoodTakeAway.getCurrentCartFoodTakeaway(context)
                var testNum = binding.tvQuantity.text.toString().toInt()
                testNum++
                binding.tvQuantity.text = testNum.toString()
                for (i in cartCurrent.food.indices){
                    if (cartCurrent.food[i].id == food.id){
                        cartCurrent.food[i].quantity += 1
                        CurrentCartFoodTakeAway.saveCurrentCartFoodTakeaway(context, cartCurrent)
                    }
                }
                detailBranch!!.onIncrease()
            }

            binding.imgDecrease.setOnClickListener {
                var testNum = binding.tvQuantity.text.toString().toInt()
                var cartCurrent = CurrentCartFoodTakeAway.getCurrentCartFoodTakeaway(context)
                testNum--
                if (testNum == 0){
                    binding.lnQuantity.visibility = View.GONE
                    binding.btnAdd.visibility = View.VISIBLE
                }
                binding.tvQuantity.text = testNum.toString()

                for (i in cartCurrent.food.indices){
                    if (cartCurrent.food[i].id == food.id){
                        cartCurrent.food[i].quantity -= 1
                        if (cartCurrent.food[i].quantity == 0){
                            cartCurrent.food.removeAt(i)
                            if (cartCurrent.food.size == 0){
                                cartCurrent = CartFoodTakeAway()
                            }
                            CurrentCartFoodTakeAway.saveCurrentCartFoodTakeaway(context, cartCurrent)
                            break
                        }
                        CurrentCartFoodTakeAway.saveCurrentCartFoodTakeaway(context, cartCurrent)
                    }
                }
                detailBranch!!.onDecrease()
            }

            binding.btnAdd.setOnClickListener {
                val cartCurrent = CurrentCartFoodTakeAway.getCurrentCartFoodTakeaway(context)
                if (branchId == cartCurrent.id_branch){
                    binding.lnQuantity.visibility = View.VISIBLE
                    binding.btnAdd.visibility = View.GONE
                    binding.tvQuantity.text = "1"
                }
                detailBranch!!.addDish(food, position)

            }
        }

    }

}