package vn.techres.line.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemViewerInformationBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.interfaces.chat.ViewerMessageHandler

class ViewerInformationAdapter(var context: Context) :
    RecyclerView.Adapter<ViewerInformationAdapter.ViewerInformationHolder>() {
    private var dataSource = ArrayList<MessageViewer>()
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var viewerMessageHandler : ViewerMessageHandler? = null

    fun setViewerMessageHandler(viewerMessageHandler : ViewerMessageHandler?){
        this.viewerMessageHandler = viewerMessageHandler
    }

    fun setDataSource(dataSource : ArrayList<MessageViewer>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewerInformationHolder {
        return ViewerInformationHolder(
            ItemViewerInformationBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewerInformationHolder, position: Int) {
        holder.bind(configNodeJs, dataSource[position], viewerMessageHandler)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    class ViewerInformationHolder(private val binding: ItemViewerInformationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(configNodeJs: ConfigNodeJs, messageViewer: MessageViewer, viewerMessageHandler : ViewerMessageHandler?){
            getImage(binding.imgAvatarUser, messageViewer.avatar?.medium, configNodeJs)
            binding.tvName.text = messageViewer.full_name ?: ""
            binding.root.setOnClickListener {
                viewerMessageHandler?.onChooseViewer(messageViewer)
            }
        }
    }
}