package vn.techres.line.interfaces

interface CreateAlbumHandler {
    fun onClick(position: Int)
    fun onOneAlbum(position: Int, id: Int)
}