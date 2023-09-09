package vn.techres.line.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.databinding.*
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.holder.message.important.*
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.ImageMoreChatHandler
import vn.techres.line.interfaces.chat.MessageImportantHandler

class MessageImportantAdapter(var context : Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private val configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var dataSource = ArrayList<MessagesByGroup>()
    private var messageImportantHandler : MessageImportantHandler? = null
    private var imageMoreChatHandler: ImageMoreChatHandler? = null
    private var chooseNameUserHandler: ChooseNameUserHandler? = null

    fun setDataSource(dataSource : ArrayList<MessagesByGroup>){
        val index = dataSource.size
        this.dataSource = dataSource
        notifyItemRangeInserted(index, dataSource.size)
    }

    fun setMessageImportantHandler(messageImportantHandler : MessageImportantHandler){
        this.messageImportantHandler = messageImportantHandler
    }

    fun setImageMoreChatHandler(imageMoreChatHandler: ImageMoreChatHandler){
        this.imageMoreChatHandler = imageMoreChatHandler
    }

    fun setChooseTagNameHandler(chooseNameUserHandler: ChooseNameUserHandler) {
        this.chooseNameUserHandler = chooseNameUserHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            MESSAGE_TEXT ->{
                TextMessageImportantHolder(ItemTextMessageImportantBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            MESSAGE_IMAGE ->{
                ImageMessageImportantHolder(ItemImageMessageImportantBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            MESSAGE_VIDEO ->{
                VideoMessageImportantHolder(ItemVideoMessageImportantBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            MESSAGE_FILE ->{
                FileMessageImportantHolder(ItemFileMessageImportantBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            MESSAGE_AUDIO ->{
                AudioMessageImportantHolder(ItemAudioMessageImportantBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            MESSAGE_BUSINESS_CARD ->{
                BusinessCardMessageImportant(ItemBusinessCardMessageImportantBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            MESSAGE_LINK ->{
                LinkMessageImportantHolder(ItemLinkMessageImportantBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            TYPE_REPLY ->{
                ReplyMessageImportantHolder(ItemTextMessageImportantBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            else ->{
                TextMessageImportantHolder(ItemTextMessageImportantBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            MESSAGE_TEXT ->{
                (holder as TextMessageImportantHolder).bind(configNodeJs, dataSource[position], messageImportantHandler, chooseNameUserHandler)
            }
            MESSAGE_IMAGE ->{
                (holder as ImageMessageImportantHolder).bind(context, configNodeJs, dataSource[position], messageImportantHandler, imageMoreChatHandler)
            }
            MESSAGE_VIDEO ->{
                (holder as VideoMessageImportantHolder).bind(context, configNodeJs, dataSource[position], messageImportantHandler, imageMoreChatHandler)
            }
            MESSAGE_FILE ->{
                (holder as FileMessageImportantHolder).bind(context, configNodeJs, dataSource[position], messageImportantHandler)
            }
            MESSAGE_AUDIO ->{
                (holder as AudioMessageImportantHolder).bind(context, configNodeJs, dataSource[position], messageImportantHandler)
            }
            MESSAGE_BUSINESS_CARD ->{
                (holder as BusinessCardMessageImportant).bind(configNodeJs, dataSource[position], messageImportantHandler)
            }
            MESSAGE_LINK ->{
                (holder as BusinessCardMessageImportant).bind(configNodeJs, dataSource[position], messageImportantHandler)
            }
            TYPE_REPLY ->{
                (holder as ReplyMessageImportantHolder).bind(configNodeJs, dataSource[position], messageImportantHandler, chooseNameUserHandler)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(dataSource[position].message_type){
            TechResEnumChat.TYPE_TEXT.toString() ->{
                MESSAGE_TEXT
            }
            TechResEnumChat.TYPE_IMAGE.toString() ->{
                MESSAGE_IMAGE
            }
            TechResEnumChat.TYPE_VIDEO.toString() ->{
                MESSAGE_VIDEO
            }
            TechResEnumChat.TYPE_AUDIO.toString() ->{
                MESSAGE_AUDIO
            }
            TechResEnumChat.TYPE_FILE.toString() ->{
                MESSAGE_FILE
            }
            TechResEnumChat.TYPE_LINK.toString() ->{
                MESSAGE_LINK
            }
            TechResEnumChat.TYPE_REPLY.toString() ->{
                TYPE_REPLY
            }
            TechResEnumChat.TYPE_BUSINESS_CARD.toString() ->{
                MESSAGE_BUSINESS_CARD
            }
            else ->{
                -1
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    companion object{
        private const val MESSAGE_TEXT = 1
        private const val MESSAGE_IMAGE = 2
        private const val MESSAGE_VIDEO = 3
        private const val MESSAGE_AUDIO = 4
        private const val MESSAGE_FILE = 5
        private const val MESSAGE_BUSINESS_CARD = 6
        private const val MESSAGE_LINK = 7
        private const val TYPE_REPLY = 8
    }
}