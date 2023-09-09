package vn.techres.line.data.model.chat.request

class BirthDayRequest{

    var background : String? = ""
    var group_id : String? = ""
    var message_type : String? = ""
    var message : String? = ""

    var member_id : Int? = 0
    var user_invited_id : Int? = 0
    var position : Position? = Position()
}
class Position {
    constructor()
    constructor(margin_top: Int?, margin_left: Int?, width: Int?, height: Int?) {
        this.margin_top = margin_top
        this.margin_left = margin_left
        this.width = width
        this.height = height
    }

    var margin_top : Int? = 0
    var margin_left : Int? = 0
    var width : Int? = 0
    var height : Int? = 0
}