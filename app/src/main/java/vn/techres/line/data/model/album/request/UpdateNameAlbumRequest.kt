package vn.techres.line.data.model.album.request

class UpdateNameAlbumRequest {
    constructor(folderName: String) {
        this.folder_name = folderName
    }

    constructor()
    var folder_name = ""
}