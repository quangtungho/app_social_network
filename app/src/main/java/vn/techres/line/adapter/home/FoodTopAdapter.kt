package vn.techres.line.adapter.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_food_top_new.view.tvNameFood
import kotlinx.android.synthetic.main.item_food_top_new.view.tvPrice
import kotlinx.android.synthetic.main.item_food_top_new.view.tvTotalRating
import kotlinx.android.synthetic.main.item_food_top_new_new.view.*
import vn.techres.line.R
import vn.techres.line.data.model.food.FoodBestSeller
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.ClickOneImage

class FoodTopAdapter(
    var context: Context
) : RecyclerView.Adapter<FoodTopAdapter.ViewHolder>() {
    private var foodBestSellerList = ArrayList<FoodBestSeller>()
    private var clickOneImage: ClickOneImage? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    fun setDataSource(foodBestSellerList: ArrayList<FoodBestSeller>) {
        this.foodBestSellerList = foodBestSellerList
        notifyDataSetChanged()
    }

    fun setClickOneImage(clickOneImage: ClickOneImage) {
        this.clickOneImage = clickOneImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_food_top_new_new,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return foodBestSellerList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


//        try {
//            Glide.with(context)
//                .load(String.format("%s%s", nodeJs.api_ads, foodBestSellerList[position].avatar))
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .centerCrop()
//                .apply(
//                    RequestOptions().placeholder(R.drawable.logo_aloline_plancehoder)
//                        .error(R.drawable.logo_aloline_plancehoder)
//                )
//                .into(holder.imgFood)

//        }catch (ex: Exception){
//        }
        holder.imgFood.load(String.format("%s%s", nodeJs.api_ads, foodBestSellerList[position].avatar?.original)){
            crossfade(true)
            scale(Scale.FIT)
            placeholder(R.drawable.logo_aloline_placeholder)
            error(R.drawable.logo_aloline_placeholder)
            transformations(RoundedCornersTransformation(10F))
            size(500, 500)
        }

        holder.tvPrice.text = String.format(
            "%s %s", " ",
            Currency.formatCurrencyDecimal(foodBestSellerList[position].price!!.toFloat())

        )
        holder.tvNameFood.text = foodBestSellerList[position].name
        holder.tvTotalRating.text = "5.0"
//        holder.itemView.setOnClickListener {
//            detailBranch!!.onDetailDish(dataTakeAway[position].branch_id!!)
//        }

        holder.imgFood.setOnClickListener {
            clickOneImage?.onClickImage(String.format("%s%s", nodeJs.api_ads, foodBestSellerList[position].avatar?.original), position)
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgFood = view.imgFood
        //        val imgSmall = view.imgSmall
        val tvNameFood = view.tvNameFood
        val tvPrice = view.tvPrice
        val tvTotalRating = view.tvTotalRating
    }

}