package vn.techres.line.interfaces

import vn.techres.line.data.model.game.Game

interface ChooseGame {
    fun onClickGame(game : Game)
}