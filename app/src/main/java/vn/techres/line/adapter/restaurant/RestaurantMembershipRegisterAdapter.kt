package vn.techres.line.adapter.restaurant

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import net.glxn.qrgen.android.QRCode
import vn.techres.line.R
import vn.techres.line.data.model.restaurant.RestaurantRegister
import vn.techres.line.databinding.ItemRestaurantRegisterBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.interfaces.ClickMembershipRegisterHandler
import java.util.*
import java.util.stream.Collectors

class RestaurantMembershipRegisterAdapter(val context: Context) :
    RecyclerView.Adapter<RestaurantMembershipRegisterAdapter.ItemHolder>() {
    private var dataSource = ArrayList<RestaurantRegister>()
    private var dataSourceTemp = ArrayList<RestaurantRegister>()
    private var clickMembershipRegisterHandler: ClickMembershipRegisterHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(restaurantRegister: ArrayList<RestaurantRegister>) {
        this.dataSource = restaurantRegister
        this.dataSourceTemp = dataSource
        notifyDataSetChanged()
    }

    fun setClickMembershipRegister(clickMembershipRegisterHandler: ClickMembershipRegisterHandler) {
        this.clickMembershipRegisterHandler = clickMembershipRegisterHandler
    }

    inner class ItemHolder(val binding: ItemRestaurantRegisterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemRestaurantRegisterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val restaurant = dataSource[position]

        val bitmap = QRCode.from(restaurant.membership_register_code).withSize(200, 200)
            .withCharset("UTF-8").bitmap()

        holder.binding.qrCode.setImageBitmap(bitmap)

        holder.binding.imgBackgroundLogo.load(String.format("%s%s", nodeJs.api_ads, restaurant.image_logo_url.medium))
        {
            crossfade(true)
            scale(Scale.FILL)
            placeholder(R.drawable.logo_aloline_placeholder)
            error(R.drawable.logo_aloline_placeholder)
            size(500, 500)
        }

        holder.binding.txtRestaurantName.text = restaurant.name
        holder.binding.txtRestaurantAddress.text = restaurant.address_full_text
        holder.binding.txtRestaurantPhone.text = restaurant.phone_number
        holder.binding.rbRestaurantRate.rating = restaurant.rating!!.toFloat()
        holder.itemView.setOnClickListener {
            restaurant.membership_register_code?.let { it1 ->
                clickMembershipRegisterHandler!!.onClickMembershipRegister(
                    position,
                    it1
                )
            }
        }
    }

    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault())
        dataSource = if (StringUtils.isNullOrEmpty(key)) {
            dataSourceTemp
        } else {
            dataSourceTemp.stream().filter {
                it.name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.restaurant_name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.address_full_text!!.lowercase(Locale.ROOT)
                    .contains(key)
            }.collect(Collectors.toList()) as ArrayList<RestaurantRegister>
        }
        notifyDataSetChanged()
    }

}