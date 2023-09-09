package vn.techres.line.data.model.album.request

class ImageAlbumRequest {
    constructor(page: Int, user_id: Int, limit : Int) {
        this.page = page
        this.user_id = user_id
        this.limit = limit
    }

    constructor()

    var page = 0
    var user_id = 0
    var limit = 0
}