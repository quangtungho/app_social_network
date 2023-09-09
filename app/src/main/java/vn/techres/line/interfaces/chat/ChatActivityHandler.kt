package vn.techres.line.interfaces.chat

interface ChatActivityHandler {
    fun onResumeChat()
    fun onPauseChat()
    fun onStopChat()
}