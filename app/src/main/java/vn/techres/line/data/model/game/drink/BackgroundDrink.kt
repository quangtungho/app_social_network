package vn.techres.line.data.model.game.drink

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class BackgroundDrink : Serializable {
    @JsonField(name = ["key"])
    var key: String = ""

    @JsonField(name = ["drawable"])
    var drawable: Int = 0

    @JsonField(name = ["link"])
    var link: String = ""
}