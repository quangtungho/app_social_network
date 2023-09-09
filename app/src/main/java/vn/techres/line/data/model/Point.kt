package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField

class Point{
    @JsonField(name = ["restaurant_image"])
    var restaurant_image: String = ""

    @JsonField(name = ["total_point"])
    var total_point: Int = 0
}