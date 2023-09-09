package vn.techres.line.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.chat.Sender
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemMemberCreateGroupBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils.getImage

class MemberCreateGroupAdapter(var context : Context) : RecyclerView.Adapter<MemberCreateGroupAdapter.ViewHolder>() {
    private var number = 0
    private var dataSources = ArrayList<Sender>()
    private val configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    fun setDataSource(dataSources : ArrayList<Sender>, number : Int){
        this.dataSources = dataSources
        this.number = number
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMemberCreateGroupBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(number, dataSources[position], configNodeJs)
    }

    override fun getItemCount(): Int {
        return dataSources.size
    }

    class ViewHolder(private val binding : ItemMemberCreateGroupBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(number: Int,sender : Sender, configNodeJs: ConfigNodeJs){
            getImage(binding.imgMember, sender.avatar?.medium, configNodeJs)
            binding.imgMore.visibility = if(bindingAdapterPosition == 4 && number > 4){
                View.VISIBLE
            }else{
                View.GONE
            }

        }
    }
}