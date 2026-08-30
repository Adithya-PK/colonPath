package com.example.colonpath_ai.model

enum class CameraConnectionStatus {
    NOT_CONNECTED, CONNECTING, CONNECTED, ERROR
}

data class CameraInfo(
    val manufacturer: String = "Not detected",
    val model: String = "Not detected",
    val resolution: String = "Not detected",
    val fps: String = "Not detected",
    val pixelFormat: String = "Not detected",
    val connectionType: String = "USB/UVC",
    val uvcStatus: String = "Not detected",
    val usbOtgStatus: String = "Not detected",
    val magnification: String = "Not available",
    val calibration: String = "Not available",
    val connectionStatus: CameraConnectionStatus = CameraConnectionStatus.NOT_CONNECTED
)
