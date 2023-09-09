package vn.techres.line.adapter.game.luckywheel

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import vn.techres.line.R
import vn.techres.line.data.model.game.Game
import vn.techres.line.databinding.ItemGameBinding
import vn.techres.line.interfaces.ChooseGame
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class GameListAdapter(var context: Context) : RecyclerView.Adapter<GameListAdapter.ViewHolder>(){
    private var dataSource: ArrayList<Game> = ArrayList()
    private val dataTemp: ArrayList<Game> = ArrayList()
    private var chooseGame : ChooseGame? = null

    fun setDataSource(dataSource: ArrayList<Game>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }
    fun setClickGame(chooseGame: ChooseGame) {
        this.chooseGame = chooseGame
    }

    fun filter(charText: String) {
        val finalCharText = charText.lowercase(Locale.getDefault())
        dataSource = if (finalCharText != "") {
            dataTemp.stream().filter { t: Game ->
                (t.name?.lowercase(Locale.getDefault())?.contains(finalCharText) == true ||
                        t.normalize_name?.lowercase(Locale.getDefault())?.contains(finalCharText) == true
                        || t.prefix?.lowercase(Locale.getDefault())?.contains(finalCharText) == true)
            }.collect(Collectors.toList()) as ArrayList<Game>
        } else dataTemp
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSource[position], chooseGame)
    }

    override fun getItemCount(): Int = dataSource.size

    class ViewHolder(private val binding : ItemGameBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(game : Game, chooseGame: ChooseGame?){
            binding.nameGame.text = game.name
            binding.countGame.text = String.format("%s %s", game.total_player, "người chơi")

            Glide.with(binding.imgGame)
                .load(game.avatar)
                .centerCrop().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .apply(
                    RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                )
                .into(binding.imgGame)

            binding.root.setOnClickListener {
                chooseGame?.onClickGame(
                    game
                )
            }

        }
    }
}