package vn.techres.line.interfaces

import vn.techres.line.data.model.utils.Media

interface MediaHandler {
    fun onMedia(url: ArrayList<Media>, position: Int)
    fun onRemoveMedia(position : Int, nameFile: String)
}
