package vn.techres.line.adapter.game.drink

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import vn.techres.line.R
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.game.DrinkGameHandler
import vn.techres.line.data.model.game.drink.BeverageList

class DrinkGameAdapter(var context: Context) :
    RecyclerView.Adapter<DrinkGameAdapter.ViewHolder>() {
    private var drinkGameHandler: DrinkGameHandler? = null
    private var dataSource = ArrayList<BeverageList>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private val drinkRight = 0
    private val drinkLeft = 1

    fun setDrinkGameHandler(drinkGameHandler: DrinkGameHandler) {
        this.drinkGameHandler = drinkGameHandler
    }
    fun setDataSource(dataSource: ArrayList<BeverageList>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == drinkLeft) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_drink_game_left, parent, false)
            ViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_drink_game_right, parent, false)
            ViewHolder(view!!)
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            drinkLeft -> {
                onBind(position, holder)
            }
            drinkRight -> {
                onBind(position, holder)
            }
        }
    }

    private fun onBind(
        position: Int,
        holder: ViewHolder
    ) {
//        Glide.with(context)
//            .load(String.format("%s%s", nodeJs.api_ads, dataSource[position].avatar))
//            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//            .skipMemoryCache(true)
//            .into(holder.imgLogoDrink)
        holder.imgLogoDrink.load(String.format("%s%s", nodeJs.api_ads, dataSource[position].avatar))
        {
            crossfade(true)
            size(500, 500)
        }
        holder.imgBackgroundDrink.setImageResource(dataSource[position].drink_background.drawable)
        holder.tvNameDrinkGame.text = dataSource[position].name
        holder.tvDescriptionDrink.text = String.format(
            "%s%s",
            dataSource[position].article_content.capacity,
            context.resources.getString(R.string.mililitre)
        )
        holder.itemView.setOnClickListener {
            drinkGameHandler!!.onChoose(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) {
            drinkLeft
        } else {
            drinkRight
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imgLogoDrink: ImageView = view.findViewById(R.id.imgLogoDrink)
        var imgBackgroundDrink: ImageView = view.findViewById(R.id.imgBackgroundDrink)
        var tvNameDrinkGame: TextView = view.findViewById(R.id.tvNameDrinkGame)
        var tvDescriptionDrink: TextView = view.findViewById(R.id.tvDescriptionDrink)
    }
}