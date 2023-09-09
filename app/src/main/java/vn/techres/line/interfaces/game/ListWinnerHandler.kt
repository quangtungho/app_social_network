package vn.techres.line.interfaces.game

import vn.techres.line.data.model.response.game.UserWinner

interface ListWinnerHandler {
    fun onUserWinner(userWinner: UserWinner)
}