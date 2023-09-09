package vn.techres.line.helper

interface SystemContactHandler {
    fun clickItem(position: Int, id: Int)
    fun clickAddFriend(position: Int, id: Int?)
    fun clickNotAcceptFriend(position: Int, avatar: String?, fullName: String?, id: Int?)
    fun clickChatFriend(position: Int, avatar: String?, fullName: String?, id: Int?)
    fun clickAvatar(url: String, position: Int)
}