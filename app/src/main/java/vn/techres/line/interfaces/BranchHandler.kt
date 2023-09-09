package vn.techres.line.interfaces

import vn.techres.line.data.model.branch.Branch

interface BranchHandler {
    fun onChooseBranch(branch: Branch)
}