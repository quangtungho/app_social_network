package vn.techres.line.interfaces

interface LikeHandler {
    fun onClickLike(position:Int, id: Int?)
    fun onClickUnLike(position:Int, id: Int?)
}