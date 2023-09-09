package vn.techres.line.interfaces.restaurant

import vn.techres.line.data.model.album.ImageFolder

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
interface MediaRestaurantAlbumHandler {
    fun clickMediaRestaurantAlbum(data: ArrayList<ImageFolder>, position: Int)
}