package vn.techres.line.interfaces

interface ClickProfile {
    fun clickProfile(position: Int, userId: Int)
    fun onAvatar(url: String, position: Int)
}