package vn.techres.line.call.helper;

import android.content.Context;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpParameters;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */
public class PeerConnectionClient {
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String VIDEO_TRACK_TYPE = "video";
    private static final String TAG = "PCRTCClient";
    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String VIDEO_CODEC_H264 = "H264";
    private static final String VIDEO_CODEC_H264_BASELINE = "H264 Baseline";
    private static final String VIDEO_CODEC_H264_HIGH = "H264 High";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final String AUDIO_CODEC_ISAC = "ISAC";
    private static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
    private static final String VIDEO_FLEXFEC_FIELDTRIAL =
            "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
    private static final String VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/";
    private static final String DISABLE_WEBRTC_AGC_FIELDTRIAL =
            "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";
    private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private static final int HD_VIDEO_WIDTH = 1280;
    private static final int HD_VIDEO_HEIGHT = 720;
    private static final int BPS_IN_KBPS = 1000;
    private static final String RTCEVENTLOG_OUTPUT_DIR_NAME = "rtc_event_log";

    // Executor thread is started once in private ctor and is used for all
    // peer connection API calls to ensure new peer connection factory is
    // created on the same thread as previously destroyed factory.
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final PCObserver pcObserver = new PCObserver();
    private final SDPObserver sdpObserver = new SDPObserver();
    private final Timer statsTimer = new Timer();
    private final EglBase rootEglBase;
    private final Context appContext;
    private final PeerConnectionParameters peerConnectionParameters;
    private final PeerConnectionEvents events;

    @Nullable
    private PeerConnectionFactory factory;
    @Nullable
    private PeerConnection peerConnection;
    @Nullable
    private AudioSource audioSource;
    @Nullable
    private SurfaceTextureHelper surfaceTextureHelper;
    @Nullable
    private VideoSource videoSource;
    private boolean preferIsac;
    private boolean videoCapturerStopped;
    private boolean isError;
    @Nullable
    private VideoSink localRender;
    @Nullable
    private List<VideoSink> remoteSinks;
    private AppRTCClient.SignalingParameters signalingParameters;
    private int videoWidth;
    private int videoHeight;
    private int videoFps;
    private MediaConstraints audioConstraints;
    private MediaConstraints sdpMediaConstraints;
    // Queued remote ICE candidates are consumed only after both local and
    // remote descriptions are set. Similarly local ICE candidates are sent to
    // remote peer after both local and remote description are set.
    @Nullable
    private List<IceCandidate> queuedRemoteCandidates;
    private boolean isInitiator;
    @Nullable
    private SessionDescription localDescription; // either offer or answer description
    @Nullable
    private VideoCapturer videoCapturer;
    // enableVideo is set to true if video should be rendered and sent.
    private boolean renderVideo = true;
    @Nullable
    private VideoTrack localVideoTrack;
    @Nullable
    private VideoTrack remoteVideoTrack;
    @Nullable
    private RtpSender localVideoSender;
    // enableAudio is set to true if audio should be sent.
    private boolean enableAudio = true;
    @Nullable
    private AudioTrack localAudioTrack;
    @Nullable
    private DataChannel dataChannel;
    private final boolean dataChannelEnabled;
    // Enable RtcEventLog.
    @Nullable
    private RtcEventLog rtcEventLog;
    // Implements the WebRtcAudioRecordSamplesReadyCallback interface and writes
    // recorded audio samples to an output file.
    @Nullable
    private RecordedAudioToFileController saveRecordedAudioToFile;

    /**
     * Peer connection parameters.
     */
    public static class DataChannelParameters {
        public final boolean ordered;
        public final int maxRetransmitTimeMs;
        public final int maxRetransmits;
        public final String protocol;
        public final boolean negotiated;
        public final int id;

        public DataChannelParameters(boolean ordered, int maxRetransmitTimeMs, int maxRetransmits,
                                     String protocol, boolean negotiated, int id) {
            this.ordered = ordered;
            this.maxRetransmitTimeMs = maxRetransmitTimeMs;
            this.maxRetransmits = maxRetransmits;
            this.protocol = protocol;
            this.negotiated = negotiated;
            this.id = id;
        }
    }

    /**
     * Peer connection parameters.
     */
    public static class PeerConnectionParameters {
        public final boolean videoCallEnabled;
        public final boolean loopback;
        public final boolean tracing;
        public final int videoWidth;
        public final int videoHeight;
        public final int videoFps;
        public final int videoMaxBitrate;
        public final String videoCodec;
        public final boolean videoCodecHwAcceleration;
        public final boolean videoFlexfecEnabled;
        public final int audioStartBitrate;
        public final String audioCodec;
        public final boolean noAudioProcessing;
        public final boolean aecDump;
        public final boolean saveInputAudioToFile;
        public final boolean useOpenSLES;
        public final boolean disableBuiltInAEC;
        public final boolean disableBuiltInAGC;
        public final boolean disableBuiltInNS;
        public final boolean disableWebRtcAGCAndHPF;
        public final boolean enableRtcEventLog;
        private final DataChannelParameters dataChannelParameters;

        public PeerConnectionParameters(boolean videoCallEnabled, boolean loopback, boolean tracing,
                                        int videoWidth, int videoHeight, int videoFps, int videoMaxBitrate, String videoCodec,
                                        boolean videoCodecHwAcceleration, boolean videoFlexfecEnabled, int audioStartBitrate,
                                        String audioCodec, boolean noAudioProcessing, boolean aecDump, boolean saveInputAudioToFile,
                                        boolean useOpenSLES, boolean disableBuiltInAEC, boolean disableBuiltInAGC,
                                        boolean disableBuiltInNS, boolean disableWebRtcAGCAndHPF, boolean enableRtcEventLog,
                                        DataChannelParameters dataChannelParameters) {
            this.videoCallEnabled = videoCallEnabled;
            this.loopback = loopback;
            this.tracing = tracing;
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            this.videoFps = videoFps;
            this.videoMaxBitrate = videoMaxBitrate;
            this.videoCodec = videoCodec;
            this.videoFlexfecEnabled = videoFlexfecEnabled;
            this.videoCodecHwAcceleration = videoCodecHwAcceleration;
            this.audioStartBitrate = audioStartBitrate;
            this.audioCodec = audioCodec;
            this.noAudioProcessing = noAudioProcessing;
            this.aecDump = aecDump;
            this.saveInputAudioToFile = saveInputAudioToFile;
            this.useOpenSLES = useOpenSLES;
            this.disableBuiltInAEC = disableBuiltInAEC;
            this.disableBuiltInAGC = disableBuiltInAGC;
            this.disableBuiltInNS = disableBuiltInNS;
            this.disableWebRtcAGCAndHPF = disableWebRtcAGCAndHPF;
            this.enableRtcEventLog = enableRtcEventLog;
            this.dataChannelParameters = dataChannelParameters;
        }
    }

    /**
     * Peer connection events.
     */
    public interface PeerConnectionEvents {
        /**
         * Callback fired once local SDP is created and set.
         */
        void onLocalDescription(final SessionDescription sdp);

        /**
         * Callback fired once local Ice candidate is generated.
         */
        void onIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once local ICE candidates are removed.
         */
        void onIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * Callback fired once connection is established (IceConnectionState is
         * CONNECTED).
         */
        void onIceConnected();

        /**
         * Callback fired once connection is disconnected (IceConnectionState is
         * DISCONNECTED).
         */
        void onIceDisconnected();

        /**
         * Callback fired once DTLS connection is established (PeerConnectionState
         * is CONNECTED).
         */
        void onConnected();

        /**
         * Callback fired once DTLS connection is disconnected (PeerConnectionState
         * is DISCONNECTED).
         */
        void onDisconnected();

        /**
         * Callback fired once peer connection is closed.
         */
        void onPeerConnectionClosed();

        /**
         * Callback fired once peer connection statistics is ready.
         */
        void onPeerConnectionStatsReady(final StatsReport[] reports);

        /**
         * Callback fired once peer connection error happened.
         */
        void onPeerConnectionError(final String description);
    }

    /**
     * Create a PeerConnectionClient with the specified parameters. PeerConnectionClient takes
     * ownership of |eglBase|.
     */
    public PeerConnectionClient(Context appContext, EglBase eglBase,
                                PeerConnectionParameters peerConnectionParameters, PeerConnectionEvents events) {
        this.rootEglBase = eglBase;
        this.appContext = appContext;
        this.events = events;
        this.peerConnectionParameters = peerConnectionParameters;
        this.dataChannelEnabled = peerConnectionParameters.dataChannelParameters != null;

        Timber.d("Preferred video codec: %s", getSdpVideoCodecName(peerConnectionParameters));

        final String fieldTrials = getFieldTrials(peerConnectionParameters);
        executor.execute(() -> {
            Timber.d("Initialize WebRTC. Field trials: %s", fieldTrials);
            PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(appContext)
                            .setFieldTrials(fieldTrials)
                            .setEnableInternalTracer(true)
                            .createInitializationOptions());
        });
    }

    /**
     * This function should only be called once.
     */
    public void createPeerConnectionFactory(PeerConnectionFactory.Options options) {
        if (factory != null) {
            throw new IllegalStateException("PeerConnectionFactory has already been constructed");
        }
        executor.execute(() -> createPeerConnectionFactoryInternal(options));
    }

    public void createPeerConnection(final VideoSink localRender, final VideoSink remoteSink,
                                     final VideoCapturer videoCapturer, final AppRTCClient.SignalingParameters signalingParameters) {
        if (peerConnectionParameters.videoCallEnabled && videoCapturer == null) {
            Timber.tag(TAG).w("Video call enabled but no video capturer provided.");
        }
        createPeerConnection(localRender, Collections.singletonList(remoteSink), videoCapturer, signalingParameters);
    }

    public void createPeerConnection(final VideoSink localRender, final List<VideoSink> remoteSinks,
                                     final VideoCapturer videoCapturer, final AppRTCClient.SignalingParameters signalingParameters) {
        if (peerConnectionParameters == null) {
            Timber.e("Creating peer connection without initializing factory.");
            return;
        }
        this.localRender = localRender;
        this.remoteSinks = remoteSinks;
        this.videoCapturer = videoCapturer;
        this.signalingParameters = signalingParameters;
        executor.execute(() -> {
            try {
                createMediaConstraintsInternal();
                createPeerConnectionInternal();
                maybeCreateAndStartRtcEventLog();
            } catch (Exception e) {
                reportError("Failed to create peer connection: " + e.getMessage());
                throw e;
            }
        });
    }

    public void close() {
        executor.execute(this::closeInternal);
    }

    private boolean isVideoCallEnabled() {
        return peerConnectionParameters.videoCallEnabled && videoCapturer != null;
    }

    private void createPeerConnectionFactoryInternal(PeerConnectionFactory.Options options) {
        isError = false;

        if (peerConnectionParameters.tracing) {
            PeerConnectionFactory.startInternalTracingCapture(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                            + "webrtc-trace.txt");
        }

        // Check if ISAC is used by default.
        preferIsac = peerConnectionParameters.audioCodec != null
                && peerConnectionParameters.audioCodec.equals(AUDIO_CODEC_ISAC);

        // It is possible to save a copy in raw PCM format on a file by checking
        // the "Save input audio to file" checkbox in the Settings UI. A callback
        // interface is set when this flag is enabled. As a result, a copy of recorded
        // audio samples are provided to this client directly from the native audio
        // layer in Java.
        if (peerConnectionParameters.saveInputAudioToFile) {
            if (!peerConnectionParameters.useOpenSLES) {
                Timber.d("Enable recording of microphone input audio to file");
                saveRecordedAudioToFile = new RecordedAudioToFileController(executor);
            } else {
                // then the "Save inut audio to file" option shall be grayed out.
                Timber.e("Recording of input audio is not supported for OpenSL ES");
            }
        }

        final AudioDeviceModule adm = createJavaAudioDevice();

        // Create peer connection factory.
        if (options != null) {
            Timber.d("Factory networkIgnoreMask option: %s", options.networkIgnoreMask);
        }
        final boolean enableH264HighProfile =
                VIDEO_CODEC_H264_HIGH.equals(peerConnectionParameters.videoCodec);
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        if (peerConnectionParameters.videoCodecHwAcceleration) {
            encoderFactory = new DefaultVideoEncoderFactory(
                    rootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, enableH264HighProfile);
            decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
        } else {
            encoderFactory = new SoftwareVideoEncoderFactory();
            decoderFactory = new SoftwareVideoDecoderFactory();
        }

        factory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
        Timber.d("Peer connection factory created.");
        adm.release();
    }

    AudioDeviceModule createJavaAudioDevice() {
        // Enable/disable OpenSL ES playback.
        if (!peerConnectionParameters.useOpenSLES) {
            Timber.tag(TAG).w("External OpenSLES ADM not implemented yet.");
        }

        // Set audio record error callbacks.
        JavaAudioDeviceModule.AudioRecordErrorCallback audioRecordErrorCallback = new JavaAudioDeviceModule.AudioRecordErrorCallback() {
            @Override
            public void onWebRtcAudioRecordInitError(String errorMessage) {
                Timber.e("onWebRtcAudioRecordInitError: " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordStartError(
                    JavaAudioDeviceModule.AudioRecordStartErrorCode errorCode, String errorMessage) {
                Timber.e("onWebRtcAudioRecordStartError: " + errorCode + ". " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordError(String errorMessage) {
                Timber.e("onWebRtcAudioRecordError: %s", errorMessage);
                reportError(errorMessage);
            }
        };

        JavaAudioDeviceModule.AudioTrackErrorCallback audioTrackErrorCallback = new JavaAudioDeviceModule.AudioTrackErrorCallback() {
            @Override
            public void onWebRtcAudioTrackInitError(String errorMessage) {
                Timber.e("onWebRtcAudioTrackInitError: %s", errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackStartError(
                    JavaAudioDeviceModule.AudioTrackStartErrorCode errorCode, String errorMessage) {
                Timber.e("onWebRtcAudioTrackStartError: " + errorCode + ". " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackError(String errorMessage) {
                Timber.e("onWebRtcAudioTrackError: %s", errorMessage);
                reportError(errorMessage);
            }
        };

        // Set audio record state callbacks.
        JavaAudioDeviceModule.AudioRecordStateCallback audioRecordStateCallback = new JavaAudioDeviceModule.AudioRecordStateCallback() {
            @Override
            public void onWebRtcAudioRecordStart() {
                Timber.i("Audio recording starts");
            }

            @Override
            public void onWebRtcAudioRecordStop() {
                Timber.i("Audio recording stops");
            }
        };

        // Set audio track state callbacks.
        JavaAudioDeviceModule.AudioTrackStateCallback audioTrackStateCallback = new JavaAudioDeviceModule.AudioTrackStateCallback() {
            @Override
            public void onWebRtcAudioTrackStart() {
                Timber.i("Audio playout starts");
            }

            @Override
            public void onWebRtcAudioTrackStop() {
                Timber.i("Audio playout stops");
            }
        };

        return JavaAudioDeviceModule.builder(appContext)
                .setSamplesReadyCallback(saveRecordedAudioToFile)
                .setUseHardwareAcousticEchoCanceler(!peerConnectionParameters.disableBuiltInAEC)
                .setUseHardwareNoiseSuppressor(!peerConnectionParameters.disableBuiltInNS)
                .setAudioRecordErrorCallback(audioRecordErrorCallback)
                .setAudioTrackErrorCallback(audioTrackErrorCallback)
                .setAudioRecordStateCallback(audioRecordStateCallback)
                .setAudioTrackStateCallback(audioTrackStateCallback)
                .createAudioDeviceModule();
    }

    private void createMediaConstraintsInternal() {
        // Create video constraints if video call is enabled.
        if (isVideoCallEnabled()) {
            videoWidth = peerConnectionParameters.videoWidth;
            videoHeight = peerConnectionParameters.videoHeight;
            videoFps = peerConnectionParameters.videoFps;

            // If video resolution is not specified, default to HD.
            if (videoWidth == 0 || videoHeight == 0) {
                videoWidth = HD_VIDEO_WIDTH;
                videoHeight = HD_VIDEO_HEIGHT;
            }

            // If fps is not specified, default to 30.
            if (videoFps == 0) {
                videoFps = 30;
            }
            Logging.d(TAG, "Capturing format: " + videoWidth + "x" + videoHeight + "@" + videoFps);
        }

        // Create audio constraints.
        audioConstraints = new MediaConstraints();
        // added for audio performance measurements
        if (peerConnectionParameters.noAudioProcessing) {
            Timber.d("Disabling audio processing");
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        }
        // Create SDP constraints.
        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", Boolean.toString(isVideoCallEnabled())));
    }

    private void createPeerConnectionInternal() {
        if (factory == null || isError) {
            Timber.e("Peerconnection factory is not created");
            return;
        }
        Timber.d("Create peer connection.");

        queuedRemoteCandidates = new ArrayList<>();

        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(signalingParameters.iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = !peerConnectionParameters.loopback;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;

        peerConnection = factory.createPeerConnection(rtcConfig, pcObserver);

        if (dataChannelEnabled) {
            DataChannel.Init init = new DataChannel.Init();
            init.ordered = peerConnectionParameters.dataChannelParameters.ordered;
            init.negotiated = peerConnectionParameters.dataChannelParameters.negotiated;
            init.maxRetransmits = peerConnectionParameters.dataChannelParameters.maxRetransmits;
            init.maxRetransmitTimeMs = peerConnectionParameters.dataChannelParameters.maxRetransmitTimeMs;
            init.id = peerConnectionParameters.dataChannelParameters.id;
            init.protocol = peerConnectionParameters.dataChannelParameters.protocol;
            assert peerConnection != null;
            dataChannel = peerConnection.createDataChannel("ApprtcDemo data", init);
        }
        isInitiator = false;

        // Set INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!
        Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO);

        List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
        if (isVideoCallEnabled()) {
            assert peerConnection != null;
            peerConnection.addTrack(createVideoTrack(videoCapturer), mediaStreamLabels);
            // We can add the renderers right away because we don't need to wait for an
            // answer to get the remote track.
            remoteVideoTrack = getRemoteVideoTrack();
            assert remoteVideoTrack != null;
            remoteVideoTrack.setEnabled(renderVideo);
            assert remoteSinks != null;
            for (VideoSink remoteSink : remoteSinks) {
                remoteVideoTrack.addSink(remoteSink);
            }
        }
        assert peerConnection != null;
        peerConnection.addTrack(createAudioTrack(), mediaStreamLabels);
        if (isVideoCallEnabled()) {
            findVideoSender();
        }

        if (peerConnectionParameters.aecDump) {
            try {
                ParcelFileDescriptor aecDumpFileDescriptor =
                        ParcelFileDescriptor.open(new File(Environment.getExternalStorageDirectory().getPath()
                                        + File.separator + "Download/audio.aecdump"),
                                ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE
                                        | ParcelFileDescriptor.MODE_TRUNCATE);
                factory.startAecDump(aecDumpFileDescriptor.detachFd(), -1);
            } catch (IOException e) {
                Timber.e(e, "Can not open aecdump file");
            }
        }

        if (saveRecordedAudioToFile != null) {
            if (saveRecordedAudioToFile.start()) {
                Timber.d("Recording input audio to file is activated");
            }
        }
        Timber.d("Peer connection created.");
    }

    private File createRtcEventLogOutputFile() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hhmm_ss", Locale.getDefault());
        Date date = new Date();
        final String outputFileName = "event_log_" + dateFormat.format(date) + ".log";
        return new File(
                appContext.getDir(RTCEVENTLOG_OUTPUT_DIR_NAME, Context.MODE_PRIVATE), outputFileName);
    }

    private void maybeCreateAndStartRtcEventLog() {
        if (appContext == null || peerConnection == null) {
            return;
        }
        if (!peerConnectionParameters.enableRtcEventLog) {
            Timber.d("RtcEventLog is disabled.");
            return;
        }
        rtcEventLog = new RtcEventLog(peerConnection);
        rtcEventLog.start(createRtcEventLogOutputFile());
    }

    private void closeInternal() {
        if (factory != null && peerConnectionParameters.aecDump) {
            factory.stopAecDump();
        }
        Timber.d("Closing peer connection.");
        statsTimer.cancel();
        if (dataChannel != null) {
            dataChannel.dispose();
            dataChannel = null;
        }
        if (rtcEventLog != null) {
            // RtcEventLog should stop before the peer connection is disposed.
            rtcEventLog.stop();
            rtcEventLog = null;
        }
        if (peerConnection != null) {
            peerConnection.dispose();
            peerConnection = null;
        }
        Timber.d("Closing audio source.");
        if (audioSource != null) {
            audioSource.dispose();
            audioSource = null;
        }
        Timber.d("Stopping capture.");
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            videoCapturerStopped = true;
            videoCapturer.dispose();
            videoCapturer = null;
        }
        Timber.d("Closing video source.");
        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }
        if (saveRecordedAudioToFile != null) {
            Timber.d("Closing audio file for recorded input audio.");
            saveRecordedAudioToFile.stop();
            saveRecordedAudioToFile = null;
        }
        localRender = null;
        remoteSinks = null;
        Timber.d("Closing peer connection factory.");
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
        rootEglBase.release();
        Timber.d("Closing peer connection done.");
        events.onPeerConnectionClosed();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
    }

    public boolean isHDVideo() {
        return isVideoCallEnabled() && videoWidth * videoHeight >= 1280 * 720;
    }

    @SuppressWarnings("deprecation")
    private void getStats() {
        if (peerConnection == null || isError) {
            return;
        }
        boolean success = peerConnection.getStats(events::onPeerConnectionStatsReady, null);
        if (!success) {
            Timber.e("getStats() returns false!");
        }
    }

    public void enableStatsEvents(boolean enable, int periodMs) {
        if (enable) {
            try {
                statsTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        executor.execute(PeerConnectionClient.this::getStats);
                    }
                }, 0, periodMs);
            } catch (Exception e) {
                Timber.e(e, "Can not schedule statistics timer");
            }
        } else {
            statsTimer.cancel();
        }
    }

    public void setAudioEnabled(final boolean enable) {
        executor.execute(() -> {
            enableAudio = enable;
            if (localAudioTrack != null) {
                localAudioTrack.setEnabled(enableAudio);
            }
        });
    }

    public void setVideoEnabled(final boolean enable) {
        executor.execute(() -> {
            renderVideo = enable;
            if (localVideoTrack != null) {
                localVideoTrack.setEnabled(renderVideo);
            }
            if (remoteVideoTrack != null) {
                remoteVideoTrack.setEnabled(renderVideo);
            }
        });
    }

    public void setVideoEnabledFontCamera(final boolean enable) {
        executor.execute(() -> {
            renderVideo = enable;
            if (localVideoTrack != null) {
                localVideoTrack.setEnabled(renderVideo);
            }
        });
    }

    public void createOffer() {
        executor.execute(() -> {
            if (peerConnection != null && !isError) {
                Timber.d("PC Create OFFER");
                isInitiator = true;
                peerConnection.createOffer(sdpObserver, sdpMediaConstraints);
            }
        });
    }

    public void createAnswer() {
        executor.execute(() -> {
            if (peerConnection != null && !isError) {
                Timber.d("PC create ANSWER");
                isInitiator = false;
                peerConnection.createAnswer(sdpObserver, sdpMediaConstraints);
            }
        });
    }

    public void addRemoteIceCandidate(final IceCandidate candidate) {
        executor.execute(() -> {
            if (peerConnection != null && !isError) {
                if (queuedRemoteCandidates != null) {
                    queuedRemoteCandidates.add(candidate);
                } else {
                    peerConnection.addIceCandidate(candidate);
                }
            }
        });
    }

    public void removeRemoteIceCandidates(final IceCandidate[] candidates) {
        executor.execute(() -> {
            if (peerConnection == null || isError) {
                return;
            }
            // Drain the queued remote candidates if there is any so that
            // they are processed in the proper order.
            drainCandidates();
            peerConnection.removeIceCandidates(candidates);
        });
    }

    public void setRemoteDescription(final SessionDescription desc) {
        executor.execute(() -> {
            if (peerConnection == null || isError) {
                return;
            }
            String sdp = desc.description;
            if (preferIsac) {
                sdp = preferCodec(sdp, AUDIO_CODEC_ISAC, true);
            }
            if (isVideoCallEnabled()) {
                sdp = preferCodec(sdp, getSdpVideoCodecName(peerConnectionParameters), false);
            }
            if (peerConnectionParameters.audioStartBitrate > 0) {
                sdp = setStartBitrate(
                        sdp, peerConnectionParameters.audioStartBitrate);
            }
            Timber.d("Set remote SDP.");
            SessionDescription sdpRemote = new SessionDescription(desc.type, sdp);
            peerConnection.setRemoteDescription(sdpObserver, sdpRemote);
        });
    }

    public void stopVideoSource() {
        executor.execute(() -> {
            if (videoCapturer != null && !videoCapturerStopped) {
                Timber.d("Stop video source.");
                try {
                    videoCapturer.stopCapture();
                } catch (InterruptedException ignored) {
                }
                videoCapturerStopped = true;
            }
        });
    }

    public void startVideoSource() {
        executor.execute(() -> {
            if (videoCapturer != null && videoCapturerStopped) {
                Timber.d("Restart video source.");
                videoCapturer.startCapture(videoWidth, videoHeight, videoFps);
                videoCapturerStopped = false;
            }
        });
    }

    public void setVideoMaxBitrate(@Nullable final Integer maxBitrateKbps) {
        executor.execute(() -> {
            if (peerConnection == null || localVideoSender == null || isError) {
                return;
            }
            Timber.d("Requested max video bitrate: %s", maxBitrateKbps);
            if (localVideoSender == null) {
                Timber.tag(TAG).w("Sender is not ready.");
                return;
            }

            RtpParameters parameters = localVideoSender.getParameters();
            if (parameters.encodings.isEmpty()) {
                Timber.tag(TAG).w("RtpParameters are not ready.");
                return;
            }

            for (RtpParameters.Encoding encoding : parameters.encodings) {
                // Null value means no limit.
                encoding.maxBitrateBps = maxBitrateKbps == null ? null : maxBitrateKbps * BPS_IN_KBPS;
            }
            if (!localVideoSender.setParameters(parameters)) {
                Timber.e("RtpSender.setParameters failed.");
            }
            Timber.d("Configured max video bitrate to: %s", maxBitrateKbps);
        });
    }

    private void reportError(final String errorMessage) {
        Timber.e("Peerconnection error: %s", errorMessage);
        executor.execute(() -> {
            if (!isError) {
                events.onPeerConnectionError(errorMessage);
                isError = true;
            }
        });
    }

    @Nullable
    private AudioTrack createAudioTrack() {
        assert factory != null;
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(enableAudio);
        return localAudioTrack;
    }

    @Nullable
    private VideoTrack createVideoTrack(VideoCapturer capturer) {
        surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
        assert factory != null;
        videoSource = factory.createVideoSource(capturer.isScreencast());
        capturer.initialize(surfaceTextureHelper, appContext, videoSource.getCapturerObserver());
        capturer.startCapture(videoWidth, videoHeight, videoFps);

        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(renderVideo);
        localVideoTrack.addSink(localRender);
        return localVideoTrack;
    }

    private void findVideoSender() {
        assert peerConnection != null;
        for (RtpSender sender : peerConnection.getSenders()) {
            if (sender.track() != null) {
                String trackType = Objects.requireNonNull(sender.track()).kind();
                if (trackType.equals(VIDEO_TRACK_TYPE)) {
                    Timber.d("Found video sender.");
                    localVideoSender = sender;
                }
            }
        }
    }

    // Returns the remote VideoTrack, assuming there is only one.
    private @Nullable
    VideoTrack getRemoteVideoTrack() {
        assert peerConnection != null;
        for (RtpTransceiver transceiver : peerConnection.getTransceivers()) {
            MediaStreamTrack track = transceiver.getReceiver().track();
            if (track instanceof VideoTrack) {
                return (VideoTrack) track;
            }
        }
        return null;
    }

    private static String getSdpVideoCodecName(PeerConnectionParameters parameters) {
        switch (parameters.videoCodec) {
            case VIDEO_CODEC_VP9:
                return VIDEO_CODEC_VP9;
            case VIDEO_CODEC_H264_HIGH:
            case VIDEO_CODEC_H264_BASELINE:
                return VIDEO_CODEC_H264;
            default:
                return VIDEO_CODEC_VP8;
        }
    }

    private static String getFieldTrials(PeerConnectionParameters peerConnectionParameters) {
        String fieldTrials = "";
        if (peerConnectionParameters.videoFlexfecEnabled) {
            fieldTrials += VIDEO_FLEXFEC_FIELDTRIAL;
            Timber.d("Enable FlexFEC field trial.");
        }
        fieldTrials += VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL;
        if (peerConnectionParameters.disableWebRtcAGCAndHPF) {
            fieldTrials += DISABLE_WEBRTC_AGC_FIELDTRIAL;
            Timber.d("Disable WebRTC AGC field trial.");
        }
        return fieldTrials;
    }

    @SuppressWarnings("StringSplitter")
    private static String setStartBitrate(
            String sdp, int bitrateKbps) {
        String[] lines = sdp.split("\r\n");
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String codecRtpMap = null;
        String regex = "^a=rtpmap:(\\d+) " + PeerConnectionClient.AUDIO_CODEC_OPUS + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i;
                break;
            }
        }
        if (codecRtpMap == null) {
            Timber.tag(TAG).w("No rtpmap for " + PeerConnectionClient.AUDIO_CODEC_OPUS + " codec");
            return sdp;
        }
        Timber.d("Found " + PeerConnectionClient.AUDIO_CODEC_OPUS + " rtpmap " + codecRtpMap + " at " + lines[rtpmapLineIndex]);

        // Check if a=fmtp string already exist in remote SDP for this codec and
        // update it with new bitrate parameter.
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                Timber.d("Found " + PeerConnectionClient.AUDIO_CODEC_OPUS + " " + lines[i]);
                lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE + "=" + (bitrateKbps * 1000);
                Timber.d("Update remote SDP line: %s", lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");
            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet;
                bitrateSet = "a=fmtp:" + codecRtpMap + " " + AUDIO_CODEC_PARAM_BITRATE + "="
                        + (bitrateKbps * 1000);
                Timber.d("Add remote SDP line: %s", bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }
        }
        return newSdpDescription.toString();
    }

    /**
     * Returns the line number containing "m=audio|video", or -1 if no such line exists.
     */
    private static int findMediaDescriptionLine(boolean isAudio, String[] sdpLines) {
        final String mediaDescription = isAudio ? "m=audio " : "m=video ";
        for (int i = 0; i < sdpLines.length; ++i) {
            if (sdpLines[i].startsWith(mediaDescription)) {
                return i;
            }
        }
        return -1;
    }

    private static String joinString(
            Iterable<? extends CharSequence> s, String delimiter, boolean delimiterAtEnd) {
        Iterator<? extends CharSequence> iter = s.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next());
        }
        if (delimiterAtEnd) {
            buffer.append(delimiter);
        }
        return buffer.toString();
    }

    private static @Nullable
    String movePayloadTypesToFront(
            List<String> preferredPayloadTypes, String mLine) {
        // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
        final List<String> origLineParts = Arrays.asList(mLine.split(" "));
        if (origLineParts.size() <= 3) {
            Timber.e("Wrong SDP media description format: %s", mLine);
            return null;
        }
        final List<String> header = origLineParts.subList(0, 3);
        final List<String> unpreferredPayloadTypes =
                new ArrayList<>(origLineParts.subList(3, origLineParts.size()));
        unpreferredPayloadTypes.removeAll(preferredPayloadTypes);
        // Reconstruct the line with |preferredPayloadTypes| moved to the beginning of the payload
        // types.
        final List<String> newLineParts = new ArrayList<>();
        newLineParts.addAll(header);
        newLineParts.addAll(preferredPayloadTypes);
        newLineParts.addAll(unpreferredPayloadTypes);
        return joinString(newLineParts, " ", false /* delimiterAtEnd */);
    }

    private static String preferCodec(String sdp, String codec, boolean isAudio) {
        final String[] lines = sdp.split("\r\n");
        final int mLineIndex = findMediaDescriptionLine(isAudio, lines);
        if (mLineIndex == -1) {
            Timber.tag(TAG).w("No mediaDescription line, so can't prefer %s", codec);
            return sdp;
        }
        // A list with all the payload types with name |codec|. The payload types are integers in the
        // range 96-127, but they are stored as strings here.
        final List<String> codecPayloadTypes = new ArrayList<>();
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        final Pattern codecPattern = Pattern.compile("^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$");
        for (String line : lines) {
            Matcher codecMatcher = codecPattern.matcher(line);
            if (codecMatcher.matches()) {
                codecPayloadTypes.add(codecMatcher.group(1));
            }
        }
        if (codecPayloadTypes.isEmpty()) {
            Timber.tag(TAG).w("No payload types with name %s", codec);
            return sdp;
        }

        final String newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex]);
        if (newMLine == null) {
            return sdp;
        }
        Timber.d("Change media description from: " + lines[mLineIndex] + " to " + newMLine);
        lines[mLineIndex] = newMLine;
        return joinString(Arrays.asList(lines), "\r\n", true /* delimiterAtEnd */);
    }

    private void drainCandidates() {
        if (queuedRemoteCandidates != null) {
            Timber.d("Add " + queuedRemoteCandidates.size() + " remote candidates");
            for (IceCandidate candidate : queuedRemoteCandidates) {
                assert peerConnection != null;
                peerConnection.addIceCandidate(candidate);
            }
            queuedRemoteCandidates = null;
        }
    }

    private void switchCameraInternal() {
        if (videoCapturer instanceof CameraVideoCapturer) {
            if (!isVideoCallEnabled() || isError) {
                Timber.e("Failed to switch camera. Video: " + isVideoCallEnabled() + ". Error : " + isError);
                return; // No video is sent or only one camera is available or error happened.
            }
            Timber.d("Switch camera");
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
            cameraVideoCapturer.switchCamera(null);
        } else {
            Timber.d("Will not switch camera, video caputurer is not a camera");
        }
    }

    public void switchCamera() {
        executor.execute(this::switchCameraInternal);
    }

    public void changeCaptureFormat(final int width, final int height, final int framerate) {
        executor.execute(() -> changeCaptureFormatInternal(width, height, framerate));
    }

    private void changeCaptureFormatInternal(int width, int height, int framerate) {
        if (!isVideoCallEnabled() || isError) {
            Timber.e("Failed to change capture format. Video: " + isVideoCallEnabled()
                    + ". Error : " + isError);
            return;
        }
        Timber.d("changeCaptureFormat: " + width + "x" + height + "@" + framerate);
        assert videoSource != null;
        videoSource.adaptOutputFormat(width, height, framerate);
    }

    // Implementation detail: observe ICE & stream changes and react accordingly.
    private class PCObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            executor.execute(() -> events.onIceCandidate(candidate));
        }

        @Override
        public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
            executor.execute(() -> events.onIceCandidatesRemoved(candidates));
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {
            Timber.d("SignalingState: %s", newState);
        }

        @Override
        public void onIceConnectionChange(final PeerConnection.IceConnectionState newState) {
            executor.execute(() -> {
                Timber.d("IceConnectionState: %s", newState);
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    events.onIceConnected();
                } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    events.onIceDisconnected();
                } else if (newState == PeerConnection.IceConnectionState.FAILED) {
                    reportError("ICE connection failed.");
                }
            });
        }

        @Override
        public void onConnectionChange(final PeerConnection.PeerConnectionState newState) {
            executor.execute(() -> {
                Timber.d("PeerConnectionState: %s", newState);
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    events.onConnected();
                } else if (newState == PeerConnection.PeerConnectionState.DISCONNECTED) {
                    events.onDisconnected();
                } else if (newState == PeerConnection.PeerConnectionState.FAILED) {
                    reportError("DTLS connection failed.");
                }
            });
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            Timber.d("IceGatheringState: %s", newState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            Timber.d("IceConnectionReceiving changed to %s", receiving);
        }

        @Override
        public void onSelectedCandidatePairChanged(CandidatePairChangeEvent event) {
            Timber.d("Selected candidate pair changed because: %s", event);
        }

        @Override
        public void onAddStream(final MediaStream stream) {
        }

        @Override
        public void onRemoveStream(final MediaStream stream) {
        }

        @Override
        public void onDataChannel(final DataChannel dc) {
            Timber.d("New Data channel %s", dc.label());

            if (!dataChannelEnabled)
                return;

            dc.registerObserver(new DataChannel.Observer() {
                @Override
                public void onBufferedAmountChange(long previousAmount) {
                    Timber.d("Data channel buffered amount changed: " + dc.label() + ": " + dc.state());
                }

                @Override
                public void onStateChange() {
                    Timber.d("Data channel state changed: " + dc.label() + ": " + dc.state());
                }

                @Override
                public void onMessage(final DataChannel.Buffer buffer) {
                    if (buffer.binary) {
                        Timber.d("Received binary msg over %s", dc);
                        return;
                    }
                    ByteBuffer data = buffer.data;
                    final byte[] bytes = new byte[data.capacity()];
                    data.get(bytes);
                    String strData = new String(bytes, StandardCharsets.UTF_8);
                    Timber.d("Got msg: " + strData + " over " + dc);
                }
            });
        }

        @Override
        public void onRenegotiationNeeded() {
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }

        @Override
        public void onAddTrack(final RtpReceiver receiver, final MediaStream[] mediaStreams) {
        }
    }

    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    private class SDPObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(final SessionDescription desc) {
            if (localDescription != null) {
                localDescription = null;
//        reportError("Multiple SDP create.");
//        return;
            }
            String sdp = desc.description;
            if (preferIsac) {
                sdp = preferCodec(sdp, AUDIO_CODEC_ISAC, true);
            }
            if (isVideoCallEnabled()) {
                sdp = preferCodec(sdp, getSdpVideoCodecName(peerConnectionParameters), false);
            }
            final SessionDescription newDesc = new SessionDescription(desc.type, sdp);
            localDescription = newDesc;
            executor.execute(() -> {
                if (peerConnection != null && !isError) {
                    Timber.d("Set local SDP from %s", desc.type);
                    peerConnection.setLocalDescription(sdpObserver, newDesc);
                }
            });
        }

        @Override
        public void onSetSuccess() {
            executor.execute(() -> {
                if (peerConnection == null || isError) {
                    return;
                }
                if (isInitiator) {
                    // For offering peer connection we first create offer and set
                    // local SDP, then after receiving answer set remote SDP.
                    if (peerConnection.getRemoteDescription() == null) {
                        // We've just set our local SDP so time to send it.
                        Timber.d("Local SDP set succesfully");
                        events.onLocalDescription(localDescription);
                    } else {
                        // We've just set remote description, so drain remote
                        // and send local ICE candidates.
                        Timber.d("Remote SDP set succesfully");
                        drainCandidates();
                    }
                } else {
                    // For answering peer connection we set remote SDP and then
                    // create answer and set local SDP.
                    if (peerConnection.getLocalDescription() != null) {
                        // We've just set our local SDP so time to send it, drain
                        // remote and send local ICE candidates.
                        Timber.d("Local SDP set succesfully");
                        events.onLocalDescription(localDescription);
                        drainCandidates();
                    } else {
                        // We've just set remote SDP - do nothing for now -
                        // answer will be created soon.
                        Timber.d("Remote SDP set succesfully");
                    }
                }
            });
        }

        @Override
        public void onCreateFailure(final String error) {
            reportError("createSDP error: " + error);
        }

        @Override
        public void onSetFailure(final String error) {
            reportError("setSDP error: " + error);
        }
    }
}
