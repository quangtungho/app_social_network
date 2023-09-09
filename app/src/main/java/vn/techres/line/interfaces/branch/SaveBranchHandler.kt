package vn.techres.line.interfaces.branch

interface SaveBranchHandler {
    fun onSaveBranch(position : Int, branchID : Int)
}