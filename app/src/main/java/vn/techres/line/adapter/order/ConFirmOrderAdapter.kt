package vn.techres.line.adapter.order

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import vn.techres.line.R
import vn.techres.line.data.model.cart.CartFoodTakeAway
import vn.techres.line.data.model.food.FoodTakeAway
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemFoodOrderConFirmBinding
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentCartFoodTakeAway
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.EditItemCartHandler

class ConFirmOrderAdapter(val context : Context) : RecyclerView.Adapter<ConFirmOrderAdapter.ViewHolder>(){
    private var data = ArrayList<FoodTakeAway>()
    private var editItemCartHandler : EditItemCartHandler? = null
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var point  = 0

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(comFormList: ArrayList<FoodTakeAway>) {
        this.data = comFormList
        notifyDataSetChanged()
    }

    fun setPoint(point: Int) {
        this.point = point
    }

    fun setEditItemCartHandler(editItemCartHandler: EditItemCartHandler){
        this.editItemCartHandler = editItemCartHandler
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFoodOrderConFirmBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, configNodeJs, data[position], position, editItemCartHandler)
    }

    class ViewHolder(private val binding : ItemFoodOrderConFirmBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(context: Context, configNodeJs: ConfigNodeJs, data : FoodTakeAway, position: Int, editItemCartHandler : EditItemCartHandler?){
            Glide.with(binding.imgFood)
                .load(String.format("%s%s", configNodeJs.api_ads, data.avatar?.medium))
                .placeholder(R.drawable.food_demo)
                .transform(CenterCrop(), RoundedCorners(10))
                .into(binding.imgFood)
            binding.tvNameFood.text = data.name
            binding.tvPrice.text = String.format("%s%s", Currency.formatCurrencyDecimal((data.price ?: 0).toFloat()), context.resources.getString(R.string.denominations))
            binding.tvQuantity.text = data.quantity.toString()
            binding.tvAmout.text = String.format("%s%s", Currency.formatCurrencyDecimal(data.quantity * data.price!!), context.resources.getString(R.string.denominations))
            binding.root.setOnClickListener {
//                editItemCartHandler?.onChooseItemCart(data)
            }

            binding.imgIncrease.setOnClickListener {
                val cartCurrent = CurrentCartFoodTakeAway.getCurrentCartFoodTakeaway(context)
                var testNum = binding.tvQuantity.text.toString().toInt()
                testNum++
                binding.tvQuantity.text = testNum.toString()
                for (i in cartCurrent.food.indices){
                    if (cartCurrent.food[i].id == data.id){
                        cartCurrent.food[i].quantity += 1
                        CurrentCartFoodTakeAway.saveCurrentCartFoodTakeaway(context, cartCurrent)
                        binding.tvAmout.text = String.format("%s%s", Currency.formatCurrencyDecimal(testNum * data.price!!), context.resources.getString(R.string.denominations))
                    }
                }
            }

            binding.imgDecrease.setOnClickListener {
                var testNum = binding.tvQuantity.text.toString().toInt()
                var cartCurrent = CurrentCartFoodTakeAway.getCurrentCartFoodTakeaway(context)
                testNum--
                cartCurrent.food[position].quantity -= 1
                binding.tvQuantity.text = testNum.toString()
                if (testNum == 0){
                    cartCurrent.food.removeAt(position)
                    if (cartCurrent.food.size == 0){
                        cartCurrent = CartFoodTakeAway()
                    }
                }
                binding.tvAmout.text = String.format("%s%s", Currency.formatCurrencyDecimal(testNum * data.price!!), context.resources.getString(R.string.denominations))
                CurrentCartFoodTakeAway.saveCurrentCartFoodTakeaway(context, cartCurrent)

                editItemCartHandler!!.onChangeItemCart(cartCurrent.food)
            }
        }
    }
}
