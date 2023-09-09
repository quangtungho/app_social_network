package vn.techres.line.adapter.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.chat.BirthDayCard
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemBirthdayCardBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.chat.ClickBirthDayCard

class ListBirthDayCartAdapter(var context : Context) : RecyclerView.Adapter<ListBirthDayCartAdapter.ViewHolder>() {
    private var dataSources = ArrayList<BirthDayCard>()
    private val configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var clickBirthDayCard : ClickBirthDayCard? = null
    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSources : ArrayList<BirthDayCard>){
        this.dataSources = dataSources
        notifyDataSetChanged()
    }
    fun setClickBirthDayCard(clickBirthDayCard: ClickBirthDayCard){
        this.clickBirthDayCard = clickBirthDayCard
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBirthdayCardBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        clickBirthDayCard?.let {
            holder.bind(dataSources[position], configNodeJs, context,
                it, dataSources, position)
        }
    }

    override fun getItemCount(): Int {
        return dataSources.size
    }

    class ViewHolder(private val binding : ItemBirthdayCardBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data : BirthDayCard, configNodeJs: ConfigNodeJs, context : Context,
                 clickBirthDayCard : ClickBirthDayCard, dataList : ArrayList<BirthDayCard>, position: Int){
            ChatUtils.callPhoto(binding.imvCard,dataList[position].greeting_card_url, configNodeJs)

            if(data.isSelected)
                binding.selected.visibility = View.VISIBLE
            else
                binding.selected.visibility = View.GONE
            itemView.setOnClickListener {
                clickBirthDayCard.onClick(position)
            }
        }
    }
}