package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField

class Link {
    @JsonField(name = ["url"])
    var url: String? = ""

    @JsonField(name = ["title"])
    var title: String? = ""

    @JsonField(name = ["description"])
    var description: String? = ""

    @JsonField(name = ["image"])
    var image: String? = ""

    @JsonField(name = ["video"])
    var canonical_url  : String? = ""
}