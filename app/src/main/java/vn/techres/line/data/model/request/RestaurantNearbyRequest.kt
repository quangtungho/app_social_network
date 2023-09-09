package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class RestaurantNearbyRequest {
    @JsonField(name=["lat"])
    var lat: Double?=null

    @JsonField(name=["lng"])
    var lng: Double?=null

    @JsonField(name=["is_for_register_membership"])
    var is_for_register_membership: Int?=null

    @JsonField(name=["limit"])
    var limit: Int?=null

    @JsonField(name=["page"])
    var page: Int?=null
}