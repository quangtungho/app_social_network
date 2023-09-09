package vn.techres.line.adapter.game.luckywheel

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.game.luckywheel.MessageGameLuckyWheel
import vn.techres.line.databinding.ItemStickerMessageLuckyWheelBinding
import vn.techres.line.databinding.ItemTextMessageLuckyWheelBinding
import vn.techres.line.helper.CurrentConfigNodeJs.getConfigNodeJs
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.holder.luckywheel.StickerMessageLuckyWheel
import vn.techres.line.holder.luckywheel.TextMessageLuckyWheel
import vn.techres.line.interfaces.MessageLuckyWheelHandler

class MessageChatGameLuckyWheelAdapter(var context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataSource: ArrayList<MessageGameLuckyWheel> = ArrayList()
    private var messageLuckyWheelHandler : MessageLuckyWheelHandler? = null
    private var configNodeJs = getConfigNodeJs(context)

    fun setDataSource(dataSource: ArrayList<MessageGameLuckyWheel>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setMessageLuckyWheelHandler(messageLuckyWheelHandler : MessageLuckyWheelHandler){
        this.messageLuckyWheelHandler = messageLuckyWheelHandler
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            TEXT ->{
                TextMessageLuckyWheel(ItemTextMessageLuckyWheelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            STICKER ->{
               StickerMessageLuckyWheel(ItemStickerMessageLuckyWheelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            else ->{
                TextMessageLuckyWheel(ItemTextMessageLuckyWheelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            TEXT ->{
                (holder as TextMessageLuckyWheel).bind(configNodeJs, dataSource[position], messageLuckyWheelHandler)
            }
            STICKER ->{
                (holder as StickerMessageLuckyWheel).bind(configNodeJs, dataSource[position], messageLuckyWheelHandler)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(dataSource[position].message_type){
            TechResEnumChat.TYPE_TEXT.toString() ->{
                TEXT
            }
            TechResEnumChat.TYPE_STICKER.toString() ->{
                STICKER
            }
            else ->{
                0
            }
        }
    }

    companion object{
        const val TEXT = 1
        const val STICKER = 2
    }
}