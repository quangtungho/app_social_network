package vn.techres.line.data.model.food.reponse

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.food.FoodBestSeller
import vn.techres.line.data.model.response.BaseResponse

class FoodBestSellerResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<FoodBestSeller>()
}