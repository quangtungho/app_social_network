package vn.techres.line.interfaces.chat

import android.view.View
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.chat.MessagesByGroup

interface ImageMoreChatHandler {
    fun onCLickImageMore(imgList : ArrayList<FileNodeJs>, position : Int)
    fun onVideoMore(imgList : ArrayList<FileNodeJs>, position : Int)
    fun onRevokeImageMore(messagesByGroup: MessagesByGroup, view : View)

}