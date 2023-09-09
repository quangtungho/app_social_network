package vn.techres.line.interfaces.newsfeed

import vn.techres.data.line.model.PostReview

interface SearchNewsFeedHandler {
    fun onClickItem(data: PostReview)
}