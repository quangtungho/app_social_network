/*
 *  Copyright 2018 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package vn.techres.line.helper.rtc_custom.util;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import org.webrtc.audio.JavaAudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule.SamplesReadyCallback;

import timber.log.Timber;
import vn.techres.line.helper.WriteLog;

/**
 * Implements the AudioRecordSamplesReadyCallback interface and writes
 * recorded raw audio samples to an output file.
 */
public class RecordedAudioToFileController implements SamplesReadyCallback {

  private static final long MAX_FILE_SIZE_IN_BYTES = 58348800L;

  private final Object lock = new Object();
  private final ExecutorService executor;
  @Nullable
  private OutputStream rawAudioFileOutputStream;
  private boolean isRunning;
  private long fileSizeInBytes;

  public RecordedAudioToFileController(ExecutorService executor) {
    Timber.d("ctor");
    this.executor = executor;
  }

  /**
   * Should be called on the same executor thread as the one provided at
   * construction.
   */
  public boolean start() {
    Timber.d("start");
    if (!isExternalStorageWritable()) {
      Timber.e("Writing to external media is not possible");
      return false;
    }
    synchronized (lock) {
      isRunning = true;
    }
    return true;
  }

  /**
   * Should be called on the same executor thread as the one provided at
   * construction.
   */
  @SuppressLint("TimberArgCount")
  public void stop() {
    Timber.d("stop");
    synchronized (lock) {
      isRunning = false;
      if (rawAudioFileOutputStream != null) {
        try {
          rawAudioFileOutputStream.close();
        } catch (IOException e) {
          e.fillInStackTrace();
          WriteLog.INSTANCE.d("Failed to close file with saved input audio:", e.getMessage() + "");
        }
        rawAudioFileOutputStream = null;
      }
      fileSizeInBytes = 0;
    }
  }

  // Checks if external storage is available for read and write.
  private boolean isExternalStorageWritable() {
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state);
  }

  // Utilizes audio parameters to create a file name which contains sufficient
  // information so that the file can be played using an external file player.
  // Example: /sdcard/recorded_audio_16bits_48000Hz_mono.pcm.
  private void openRawAudioOutputFile(int sampleRate, int channelCount) {
    final String fileName = Environment.getExternalStorageDirectory().getPath() + File.separator
        + "recorded_audio_16bits_" + sampleRate + "Hz"
        + ((channelCount == 1) ? "_mono" : "_stereo") + ".pcm";
    final File outputFile = new File(fileName);
    try {
      rawAudioFileOutputStream = new FileOutputStream(outputFile);
    } catch (FileNotFoundException e) {
      Timber.e("Failed to open audio output file: %s", e.getMessage());
    }
    Timber.d("Opened file for recording: %s", fileName);
  }

  // Called when new audio samples are ready.
  @Override
  public void onWebRtcAudioRecordSamplesReady(JavaAudioDeviceModule.AudioSamples samples) {
    // The native audio layer on Android should use 16-bit PCM format.
    if (samples.getAudioFormat() != AudioFormat.ENCODING_PCM_16BIT) {
      Timber.e("Invalid audio format");
      return;
    }
    synchronized (lock) {
      // Abort early if stop() has been called.
      if (!isRunning) {
        return;
      }
      // Open a new file for the first callback only since it allows us to add audio parameters to
      // the file name.
      if (rawAudioFileOutputStream == null) {
        openRawAudioOutputFile(samples.getSampleRate(), samples.getChannelCount());
        fileSizeInBytes = 0;
      }
    }
    // Append the recorded 16-bit audio samples to the open output file.
    executor.execute(() -> {
      if (rawAudioFileOutputStream != null) {
        try {
          // Set a limit on max file size. 58348800 bytes corresponds to
          // approximately 10 minutes of recording in mono at 48kHz.
          if (fileSizeInBytes < MAX_FILE_SIZE_IN_BYTES) {
            // Writes samples.getData().length bytes to output stream.
            rawAudioFileOutputStream.write(samples.getData());
            fileSizeInBytes += samples.getData().length;
          }
        } catch (IOException e) {
          Timber.e("Failed to write audio to file: %s", e.getMessage());
        }
      }
    });
  }
}
