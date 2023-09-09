package vn.techres.line.data.model.album

import android.view.View

interface ClickImageAlbum {
    fun clickImageAlbum(link: List<ImageFolder>, position: Int)
    fun clickImageAlbumMore(imageFolder: ImageFolder, view: View)
}