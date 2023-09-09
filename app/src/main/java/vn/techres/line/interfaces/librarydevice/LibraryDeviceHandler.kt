package vn.techres.line.interfaces.librarydevice

import vn.techres.line.data.model.librarydevice.FileDevice

interface LibraryDeviceHandler {
    fun onChooseCamera()
    fun onChooseFile(file : FileDevice)
}