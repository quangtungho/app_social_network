package vn.techres.line.adapter.gift

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.gift.Gift
import vn.techres.line.databinding.ItemReceivingGiftBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.gift.GiftHandler

/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/14/2022
 */
class ReceivingGiftsAdapter(val context: Context) :
    RecyclerView.Adapter<ReceivingGiftsAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Gift>()
    private var giftHandler: GiftHandler? = null

    @SuppressLint("UseRequireInsteadOfGet")
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(dataSource: ArrayList<Gift>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickGift(giftHandler: GiftHandler) {
        this.giftHandler = giftHandler
    }

    inner class ItemHolder(val binding: ItemReceivingGiftBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemReceivingGiftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val data = dataSource[position]

            binding.imgGift.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    data.restaurant_gift_image_url
                )
            )
            {
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                size(500, 500)
            }

            if (data.food.size == 0) {
                binding.txtGiftName.text = data.name
            } else {
                var nameFoods = ""
                for (i in data.food.indices) {
                    if (i != 0) {
                        nameFoods = nameFoods + ", " + data.food[i].food_name
                    }
                }
                binding.txtGiftName.text = String.format("%s - %s", data.name, nameFoods)
            }

            binding.txtExpiry.text = String.format("%s: %s", "HSD", data.expire_at)


            binding.btnGetItNow.setOnClickListener {
                giftHandler!!.onGetGift(data)
            }
        }

    }

}