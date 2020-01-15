package com.baidu.ai.speech.realtime.full.upload;

import com.baidu.ai.speech.realtime.Util;
import com.baidu.ai.speech.realtime.full.util.Stat;

import okhttp3.WebSocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * 非实时流，如文件流的上传,注意数据帧之间需要有间隔
 */
public class SimpleUploader extends AbstractUploader {

    private static Logger logger = Logger.getLogger("SimpleUploader");

    /**
     * @param inputStream 非实时流，如文件流
     * @param stat        统计信息
     */
    public SimpleUploader(InputStream inputStream, Stat stat) {
        super(inputStream, stat);
    }

    /**
     * STEP 2.2 实时发送音频数据帧
     * 注意数据帧之间需要有间隔
     *
     * @param webSocket WebSocket类
     */
    @Override
    protected void sendAudioFrames(WebSocket webSocket) {
        logger.info("begin to send DATA frames");
        int bytesPerFrame = Util.BYTES_PER_FRAME;  // 一个帧 160ms的音频数据
        byte[] buffer = new byte[bytesPerFrame];
        int readSize;
        long nextFrameSendTime = System.currentTimeMillis();
        int totalSize = 0;
        do {
            if (isClosed) {
                logger.severe("websocket has been closed");
                break;
            }
            //  注意数据帧之间需要有间隔
            Util.sleep(nextFrameSendTime - System.currentTimeMillis());
            try {
                readSize = inputStream.read(buffer);
            } catch (IOException | RuntimeException e) {
                logger.warning("inputstream is closed:" + e.getClass().getSimpleName() + ":" + e.getMessage());
                readSize = -2;
            }

            if (readSize >= 0) {
                totalSize += readSize;
                logger.finer("should wait to send next DATA Frame: " + Util.bytesToTime(readSize)
                        + "ms | send binary bytes size :" + readSize + " | total size : " + totalSize);
            }
            int sleepMs = sendBytes(webSocket, buffer, readSize);
            nextFrameSendTime = System.currentTimeMillis() + sleepMs;
            /* 打开注释测试cancel逻辑
             if (totalSize > 97000){
             sendCancelFrame(webSocket);
             // 不建议使用websocket.cancel()强行断开
             break;
             }*/
        } while (readSize >= 0);
    }


}
