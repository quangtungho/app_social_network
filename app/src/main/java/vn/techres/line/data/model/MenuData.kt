package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.booking.FoodMenu
import java.io.Serializable
import java.util.ArrayList


@JsonObject
class MenuData : Serializable{
    @JsonField(name = ["list"])
    var list =  ArrayList<FoodMenu>()


}