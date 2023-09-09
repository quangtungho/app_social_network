package vn.techres.line.interfaces.contact

interface ContactNumberHandler {
    fun onDismissDialog()
    fun onNumberChoose(number : Int)
}