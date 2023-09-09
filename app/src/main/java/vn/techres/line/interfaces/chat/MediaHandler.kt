package vn.techres.line.interfaces.chat

import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.chat.LinkMessage

interface MediaHandler {
    fun onClickMedia(list : ArrayList<FileNodeJs>, position : Int)
    fun onReSendLink(linkMessage: LinkMessage)
    fun onOpenFile(path : String?)
    fun onDeleteDownLoadFile(nameFile : String?)
    fun onSuccessDownload()
}