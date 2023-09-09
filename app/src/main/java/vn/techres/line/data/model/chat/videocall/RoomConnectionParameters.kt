package vn.techres.line.data.model.chat.videocall

class RoomConnectionParameters() {
    var roomUrl: String? = ""
    var roomId: String? = ""
    var loopback = false
    var urlParameters: String? = ""
    constructor(
        roomUrl: String?, roomId: String?, loopback: Boolean, urlParameters: String?
    ) : this() {
        this.roomUrl = roomUrl
        this.roomId = roomId
        this.loopback = loopback
        this.urlParameters = urlParameters
    }
}