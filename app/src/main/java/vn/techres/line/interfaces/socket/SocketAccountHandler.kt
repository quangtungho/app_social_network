package vn.techres.line.interfaces.socket

import vn.techres.line.data.model.account.LoginWarning

interface SocketAccountHandler {
    fun onLoginWarning(loginWarning : LoginWarning)
}