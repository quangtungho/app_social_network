package vn.techres.line.interfaces

interface ListFriendHandler {
    fun clickFriendProfile(position: Int, id: Int)
    fun clickAddFriend(position: Int, avatar: String?, fullName: String?, id: Int)
    fun clickFriendChat(position: Int, avatar: String?, fullName: String?, id: Int)
}