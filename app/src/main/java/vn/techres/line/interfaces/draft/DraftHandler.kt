package vn.techres.line.interfaces.draft

import vn.techres.data.line.model.PostReview

interface DraftHandler {
    fun onPostDraft(draf: PostReview, position: Int)
    fun onEditDraft(draf: PostReview, position: Int , type: Int)
    fun onDeleteDraft(draf: PostReview, position: Int)
}