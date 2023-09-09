package vn.techres.line.interfaces

import vn.techres.line.data.model.mediaprofile.MediaProfile

interface ClickMediaProfile {
    fun onClickMedia(url: ArrayList<MediaProfile>, position: Int)
    fun onLongClickMedia(id: Int, position: Int)
}