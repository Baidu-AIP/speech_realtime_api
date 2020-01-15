package com.baidu.ai.speech.realtime.android;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.InputStream;

/**
 * 录音流
 * <p>
 * Created by fujiayi on 2017/11/27.
 */
public class MyMicrophoneInputStream extends InputStream {

    private static AudioRecord audioRecord;

    private static MyMicrophoneInputStream is;

    private volatile boolean isStarted = false;

    private volatile boolean isClosed;

    private static final String TAG = "MyMicrophoneInputStream";

    private MyMicrophoneInputStream() {

    }

    public static MyMicrophoneInputStream getInstance() {
        if (is == null) {
            synchronized (MyMicrophoneInputStream.class) {
                if (is == null) {
                    is = new MyMicrophoneInputStream();
                }
            }
        }
        is.isClosed = false;
        return is;
    }

    private void start() {
        Log.i(TAG, " MyMicrophoneInputStream start recoding!");
        if (audioRecord == null) {
            int bufferSize = AudioRecord.getMinBufferSize(16000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 16;
            // 16000 采样率 16bits 单声道
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        }
        if (audioRecord == null
                || audioRecord.getState() != AudioRecord.STATE_INITIALIZED ) {
            throw new IllegalStateException(
                    "startRecording() called on an uninitialized AudioRecord." + (audioRecord == null));
        }
        audioRecord.startRecording();
        isStarted = true;

        Log.i(TAG, " MyMicrophoneInputStream start recoding finished");
    }

    @Override
    public int read() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (!isStarted && !isClosed) {
            start(); // 建议在CALLBACK_EVENT_ASR_READY事件中调用。
            isStarted = true;
        }
        try {
            return audioRecord.read(b, off, len);
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
            throw e;
        }
    }

    /**
     * 关闭录音流
     */
    @Override
    public void close() {
        Log.i(TAG, " MyMicrophoneInputStream close");
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release(); // 程序结束别忘记自行释放
            isStarted = false;
            isClosed = true;
            audioRecord = null;
        }
    }
}
