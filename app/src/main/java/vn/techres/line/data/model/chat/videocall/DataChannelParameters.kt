package vn.techres.line.data.model.chat.videocall

class DataChannelParameters() {
    var ordered = false
    var maxRetransmitTimeMs = 0
    var maxRetransmits = 0
    var protocol: String? = ""
    var negotiated = false
    var id = 0
    constructor(
        ordered: Boolean, maxRetransmitTimeMs: Int, maxRetransmits: Int,
        protocol: String?, negotiated: Boolean, id: Int
    ) : this(){
        this.ordered = ordered
        this.maxRetransmitTimeMs = maxRetransmitTimeMs
        this.maxRetransmits = maxRetransmits
        this.protocol = protocol
        this.negotiated = negotiated
        this.id = id
    }
}