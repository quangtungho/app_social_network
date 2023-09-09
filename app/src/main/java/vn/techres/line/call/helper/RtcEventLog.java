package vn.techres.line.call.helper;

import android.os.ParcelFileDescriptor;

import org.webrtc.PeerConnection;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */
public class RtcEventLog {
    private static final int OUTPUT_FILE_MAX_BYTES = 10_000_000;
    private final PeerConnection peerConnection;
    private RtcEventLogState state = RtcEventLogState.INACTIVE;

    enum RtcEventLogState {
        INACTIVE,
        STARTED,
        STOPPED,
    }

    public RtcEventLog(PeerConnection peerConnection) {
        if (peerConnection == null) {
            throw new NullPointerException("The peer connection is null.");
        }
        this.peerConnection = peerConnection;
    }

    public void start(final File outputFile) {
        if (state == RtcEventLogState.STARTED) {
            Timber.e("RtcEventLog has already started.");
            return;
        }
        final ParcelFileDescriptor fileDescriptor;
        try {
            fileDescriptor = ParcelFileDescriptor.open(outputFile,
                    ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE
                            | ParcelFileDescriptor.MODE_TRUNCATE);
        } catch (IOException e) {
            Timber.e(e, "Failed to create a new file");
            return;
        }

        // Passes ownership of the file to WebRTC.
        boolean success =
                peerConnection.startRtcEventLog(fileDescriptor.detachFd(), OUTPUT_FILE_MAX_BYTES);
        if (!success) {
            Timber.e("Failed to start RTC event log.");
            return;
        }
        state = RtcEventLogState.STARTED;
        Timber.d("RtcEventLog started.");
    }

    public void stop() {
        if (state != RtcEventLogState.STARTED) {
            Timber.e("RtcEventLog was not started.");
            return;
        }
        peerConnection.stopRtcEventLog();
        state = RtcEventLogState.STOPPED;
        Timber.d("RtcEventLog stopped.");
    }
}
