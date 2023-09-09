package vn.techres.line.interfaces.branch

import vn.techres.line.data.model.branch.BranchDetail

interface BranchDetailHandler {
    fun clickPostReview()
    fun clickCheckIn()
    fun seeMore(branchDetail: BranchDetail?)
    fun clickSaveBranch(list: ArrayList<Int>)
}