package vn.techres.line.widget

import vn.techres.line.data.model.chat.ReactionItem

class NumberComparator : Comparator<ReactionItem> {
    override fun compare(p0: ReactionItem?, p1: ReactionItem?): Int {
        return Integer.compare(p1?.number!!, p0?.number!!)
    }
}