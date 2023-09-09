package vn.techres.line.helper.fresco.model

import java.io.Serializable

class MediaSource : Serializable {
    var url : String = ""
    var type : Int = 0
    var height : Int = 0
    var with : Int = 0
    constructor(url: String, type: Int, height: Int, with: Int) {
        this.url = url
        this.type = type
        this.height = height
        this.with = with
    }
    constructor(){}
}