package vn.techres.line.interfaces

interface ClickFriendHandler {
    fun clickFriendProfile(position: Int, id: Int)
    fun clickFriendChat(position: Int, type: String)
}