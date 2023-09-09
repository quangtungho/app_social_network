package vn.techres.line.data.model.booking

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.util.ArrayList
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BookingData : Serializable {
    @JsonField(name = ["list"])
    var list =  ArrayList<Booking>()
    @JsonField(name = ["limit"])
    var limit : Int? = 0
    @JsonField(name = ["total_record"])
    var total_record : Int? = 0
}