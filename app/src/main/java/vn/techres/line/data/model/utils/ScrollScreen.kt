package vn.techres.line.data.model.utils

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

class ScrollScreen : Serializable {
    @JsonProperty("click")
    var click: Int = 0

    @JsonProperty("currentPage")
    var currentPage: Int = 0
}