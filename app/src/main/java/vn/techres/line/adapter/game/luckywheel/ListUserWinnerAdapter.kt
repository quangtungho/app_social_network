package vn.techres.line.adapter.game.luckywheel

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.response.game.UserWinner
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemListUserWinnerBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getAvatarCircle
import vn.techres.line.interfaces.game.ListWinnerHandler
import java.util.ArrayList

class ListUserWinnerAdapter(var context: Context) : RecyclerView.Adapter<ListUserWinnerAdapter.UserWinnerHolder>() {
    private var dataSource: ArrayList<UserWinner> = ArrayList()
    private var listWinnerHandler : ListWinnerHandler? = null
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<UserWinner>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setListWinnerHandler(listWinnerHandler : ListWinnerHandler){
        this.listWinnerHandler = listWinnerHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserWinnerHolder {
        return UserWinnerHolder(ItemListUserWinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserWinnerHolder, position: Int) {
        holder.bind(context, configNodeJs, dataSource[position], listWinnerHandler)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    class UserWinnerHolder(var binding : ItemListUserWinnerBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(context: Context, configNodeJs: ConfigNodeJs, userWinner: UserWinner, listWinnerHandler : ListWinnerHandler?){
            getAvatarCircle(binding.imgAvatar, userWinner.avatar?.medium, configNodeJs)
            binding.tvName.text = userWinner.full_name
            binding.tvNameGift.text = String.format("%s: %s", context.resources.getString(R.string.reward), userWinner.gift_name)
            binding.tvTimeWinner.text = String.format("Vào lúc %s", TimeFormatHelper.getTimeListGame(userWinner.created_at))
            binding.root.setOnClickListener {
                listWinnerHandler?.onUserWinner(userWinner)
            }
        }
    }

}