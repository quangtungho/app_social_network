package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField

class CommentNewFeedData {
    @JsonField(name = ["list"])
    var list =  ArrayList<CommentNewFeed>()
}