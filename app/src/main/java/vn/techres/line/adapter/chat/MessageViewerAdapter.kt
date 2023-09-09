package vn.techres.line.adapter.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.databinding.ItemMessageViewerBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.holder.message.MessageViewerHolder

class MessageViewerAdapter(var context: Context) : RecyclerView.Adapter<MessageViewerHolder>(){
    private var dataSource = ArrayList<MessageViewer>()
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource : ArrayList<MessageViewer>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewerHolder {
        return MessageViewerHolder(ItemMessageViewerBinding.inflate(
            LayoutInflater.from(context), parent, false
        ))
    }

    override fun onBindViewHolder(holder: MessageViewerHolder, position: Int) {
        holder.bind(dataSource, position, configNodeJs)
    }

    override fun getItemCount(): Int {
       return dataSource.size
    }
}