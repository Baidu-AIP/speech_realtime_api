package com.baidu.ai.speech.realtime.full.upload;

import com.baidu.ai.speech.realtime.Util;
import com.baidu.ai.speech.realtime.full.util.Stat;

import okhttp3.WebSocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;


/**
 * WebSocket的上行逻辑
 * 实时流，由于音频数据生成的时候已经有间隔，发送时不需要再有间隔
 */
public class RealTimeUploader extends AbstractUploader {

    private static Logger logger = Logger.getLogger("RealTimeUploader");

    public RealTimeUploader(InputStream inputStream, Stat stat) {
        super(inputStream, stat);
        // 如果输送音频数据可以自己控制的话，ArrayBlockingQueue是一个更好的选择
    }


    /**
     * STEP 2.2 实时发送音频数据帧
     * <p>
     * 发送二进制，积累到160ms，即5120个字节再发送
     *
     * @param webSocket WebSocket类
     * @throws IOException
     */
    protected void sendAudioFrames(WebSocket webSocket) {
        int bytesPerFrame = Util.BYTES_PER_FRAME;  // 一个帧 160ms的音频数据
        byte[] buffer = new byte[bytesPerFrame];
        int readSize = -1;
        int totalSize = 0;

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        do {
            if (isClosed) {
                logger.severe("websocket is closed before all data sent");
                break;
            }
            try {
                readSize = inputStream.read(buffer);
            } catch (IOException | RuntimeException e) {
                logger.warning("inputstream is closed:" + e.getClass().getSimpleName() + ":" + e.getMessage());
                readSize = -2;
            }
            if (readSize > 0) {
                // 发送二进制，积累到160ms，即5120个字节再发送
                outputBuffer.write(buffer, 0, readSize);
                if (outputBuffer.size() >= bytesPerFrame) {
                    byte[] buf = outputBuffer.toByteArray();
                    sendBytes(webSocket, buf);
                    outputBuffer.reset();
                    outputBuffer.write(buf, bytesPerFrame, buf.length - bytesPerFrame);
                    totalSize += bytesPerFrame;
                    logger.finer("should wait to send next DATA Frame: " + Util.FRAME_MS
                            + "ms | send binary bytes size :" + bytesPerFrame + " | total size: " + totalSize);
                }
            } else if (readSize == -1) {
                byte[] buf = outputBuffer.toByteArray();
                if (buf.length > 0) {
                    sendBytes(webSocket, buf);
                    totalSize += buf.length;
                    logger.finer("last pack send size " + buf.length + " | total size :" + totalSize);
                }
            }
        } while (readSize >= 0);
    }


}
