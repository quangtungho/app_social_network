package vn.techres.line.interfaces

import vn.techres.line.data.model.utils.Media

interface ClickMedia {
    fun onClickImageMedia(url: ArrayList<Media>, position: Int)
}