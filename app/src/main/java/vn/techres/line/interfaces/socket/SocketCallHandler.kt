package vn.techres.line.interfaces.socket

import org.webrtc.IceCandidate
import vn.techres.line.data.model.chat.videocall.response.*

interface SocketCallHandler {
    fun onRejectCallPersonal(rejectVideoCallResponse : RejectVideoCallResponse)
    fun onAcceptCallPersonal(acceptVideoCallResponse : AcceptVideoCallResponse)
    fun onReConnectCallPersonal(connectPersonalResponse: ConnectCallPersonalResponse)
    fun onCancelCallPersonal(cancelConnectPersonalResponse: CancelConnectPersonalResponse)
    fun onOfferCallPersonal(createOfferResponse: CreateOfferResponse)
    fun onAnswerCallPersonal(createAnswerResponse: CreateAnswerResponse)
    fun onIceCandidatePersonal(iceCandidate: IceCandidate)
    fun onConnectCamera(changeCameraCallResponse: ChangeCameraCallResponse)
    fun onAcceptCamera(changeCameraCallResponse: ChangeCameraCallResponse)
    fun onRejectCamera(changeCameraCallResponse: ChangeCameraCallResponse)
    fun onTurnCamera(turnCameraResponse: TurnCameraResponse)
}