package vn.techres.line.adapter.chat.vote

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemCreateVoteBinding
import vn.techres.line.holder.vote.CreateVoteMessageHolder
import vn.techres.line.interfaces.chat.CreateVoteMessageHandler
import vn.techres.line.data.model.chat.OptionVote

class CreateVoteMessageAdapter(var context: Context) : RecyclerView.Adapter<CreateVoteMessageHolder>() {
    private var dataSources = ArrayList<OptionVote>()
    private lateinit var createVoteMessageHandler : CreateVoteMessageHandler

    fun setCreateVoteMessageHandler(createVoteMessageHandler : CreateVoteMessageHandler){
        this.createVoteMessageHandler = createVoteMessageHandler
    }
    fun setDataSource(dataSources : ArrayList<OptionVote>){
        this.dataSources = dataSources
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateVoteMessageHolder {
        return CreateVoteMessageHolder(ItemCreateVoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CreateVoteMessageHolder, position: Int) {
        holder.bind(context, dataSources[position], createVoteMessageHandler)
        holder.binding.edtPlan.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                dataSources[position].content = (s ?: "").toString()
            }
        })
    }

    override fun getItemCount(): Int {
        return dataSources.size
    }
    fun getDataSource() : ArrayList<OptionVote>{
        return dataSources
    }
}