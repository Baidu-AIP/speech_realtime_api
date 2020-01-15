package com.baidu.ai.speech.realtime;

public class Util {

    public static final int BYTES_PER_MS = 16000 * 2 / 1000; // 16000的采样率，16bits=2bytes， 1000ms
    public static final int FRAME_MS = 160; // websocket一个数据帧 160ms
    public static final int BYTES_PER_FRAME = BYTES_PER_MS * FRAME_MS; // 一个数据帧的大小=5120bytes

    /**
     * 毫秒转为字节数
     *
     * @param durationMs 毫秒
     * @return 字节数
     */
    public static long timeToBytes(long durationMs) {
        return durationMs * BYTES_PER_MS;
    }

    /**
     * 字节数转为毫秒
     *
     * @param size 字节数
     * @return 毫秒
     */
    public static int bytesToTime(int size) {
        return size / BYTES_PER_MS;
    }

    /**
     * sleep， 转为RuntimeException
     *
     * @param millis 毫秒
     */
    public static void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
