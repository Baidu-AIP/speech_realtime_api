package com.baidu.ai.speech.realtime.full.upload;

import com.baidu.ai.speech.realtime.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Logger;

public class RealTimeAudioFeeder {
    private volatile PipedInputStream pipedInputStream;

    private InputStream inputStream;

    private PipedOutputStream pipedOutputStream;

    private static Logger logger = Logger.getLogger("RealTimeAudioFeeder");

    public RealTimeAudioFeeder(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        pipedInputStream = new PipedInputStream();
        pipedOutputStream = new PipedOutputStream(pipedInputStream);
    }

    public InputStream getRealTimeInputStream() {
        return pipedInputStream;
    }

    public void startFeed() throws IOException {
        byte[] buffer = new byte[Util.BYTES_PER_FRAME];
        int readSize;

        do {
            try {
                readSize = inputStream.read(buffer);
            } catch (IOException | RuntimeException e) {
                logger.warning("inputstream is closed:" + e.getClass().getSimpleName() + ":" + e.getMessage());
                readSize = -2;
            }
            if (readSize > 0) {
                int sleepMs = Util.bytesToTime(readSize); // 数据帧之间都要sleep 读取的音频时长，即160ms；
                logger.finer("should wait to send next Frame: " + sleepMs
                        + "ms | feeded binary bytes size :" + readSize);
                pipedOutputStream.write(buffer, 0, readSize);
                pipedOutputStream.flush();
                Util.sleep(sleepMs);
            } else if (readSize == 0) {
                logger.severe("read size is 0");
                Util.sleep(100);
            }
        } while (readSize >= 0);
        pipedOutputStream.close();
    }


}
