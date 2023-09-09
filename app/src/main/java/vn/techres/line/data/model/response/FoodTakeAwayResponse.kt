package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.food.FoodTakeAway

class FoodTakeAwayResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = ArrayList<FoodTakeAway>()
}