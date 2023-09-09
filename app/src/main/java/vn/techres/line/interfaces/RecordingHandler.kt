package vn.techres.line.interfaces

interface RecordingHandler {
    fun onRecordingStarted()
    fun onRecordingLocked()
    fun onRecordingCompleted()
    fun onRecordingCanceled()
}