package vn.techres.line.adapter.delivery

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemDeliveryFoodBinding

class DeliveryFoodAdapter(var context: Context) : RecyclerView.Adapter<DeliveryFoodAdapter.DeliveryFoodHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryFoodHolder {
        return DeliveryFoodHolder(ItemDeliveryFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(foodHolder: DeliveryFoodHolder, position: Int) {
        foodHolder.bind()
    }

    override fun getItemCount(): Int {
        return 0
    }

    class DeliveryFoodHolder(val binding : ItemDeliveryFoodBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(){

        }
    }

}