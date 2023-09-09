package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField

class EventBusMap() {
    @JsonField(name = ["lat"])
    var latitude: Double = 0.0

    @JsonField(name = ["lng"])
    var longitude: Double = 0.0

    @JsonField(name = ["name_address"])
    var name_address: String = ""

    @JsonField(name = ["address_full_text"])
    var address_full_text: String = ""

    @JsonField(name = ["note"])
    var note: String = ""

    constructor(latitude: Double, longitude: Double, name_address: String, address_full_text: String, note: String) : this() {
        this.latitude = latitude
        this.longitude = longitude
        this.name_address = name_address
        this.address_full_text = address_full_text
        this.note = note
    }
}