package com.baidu.ai.speech.realtime.full.connection;

import com.baidu.ai.speech.realtime.Const;
import com.baidu.ai.speech.realtime.full.download.SimpleDownloader;
import com.baidu.ai.speech.realtime.full.upload.AbstractUploader;
import com.baidu.ai.speech.realtime.full.upload.RealTimeAudioFeeder;
import com.baidu.ai.speech.realtime.full.upload.RealTimeUploader;
import com.baidu.ai.speech.realtime.full.upload.SimpleUploader;
import com.baidu.ai.speech.realtime.full.util.Stat;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 完整示例运行类
 * STEP 1. 连接
 * STEP 2. 连接成功后发送数据
 * STEP 3. 关闭连接
 */
public class Runner {

    /**
     * 多个并发也只需要一个client
     */
    private final OkHttpClient client;

    /**
     * 输入音频源
     */
    private InputStream inputStream;

    /**
     * WebSocket库回调类
     */
    private MyWebSocketListener listener;

    /**
     * 生成最后的统计信息
     */
    private Stat stat;

    /**
     * 三个值的一个：MODE_FILE_STREAM MODE_REAL_TIME_STREAM MODE_SIMULATE_REAL_TIME_STREAM
     */
    private int mode;

    /**
     * inputStream为非实时流，如文件流，数据已经完整地在流中
     */
    public static final int MODE_FILE_STREAM = 1;

    /**
     * inputStream为实时流，如录音数据流，数据实时生成，实时被读取
     */
    public static final int MODE_REAL_TIME_STREAM = 2;

    /**
     * inputStream为非实时流，用来生成模拟实时流
     */
    public static final int MODE_SIMULATE_REAL_TIME_STREAM = 3;


    private static Logger logger = Logger.getLogger("Runner");

    /**
     * @param inputStream 输入的音频流
     * @param mode        三个MODE常量中的一个
     */
    public Runner(InputStream inputStream, int mode) {
        client = new OkHttpClient.Builder().connectTimeout(2000, TimeUnit.MILLISECONDS).build();
        this.inputStream = inputStream;
        stat = new Stat();
        this.mode = mode;
    }

    public void run() throws IOException {
        String url = Const.URI + "?sn=" + UUID.randomUUID().toString();
        logger.info("runner begin: " + url);
        Request request = new Request.Builder().url(url).build();

        // STEP 2 中的接收数据逻辑
        SimpleDownloader downloader = new SimpleDownloader();

        // STEP 2 中的发送数据逻辑
        AbstractUploader uploader;
        switch (mode) {
            case MODE_FILE_STREAM: {
                // 非实时流，如文件流上传，帧之间需要有间隔
                uploader = new SimpleUploader(inputStream, stat);
                break;
            }

            case MODE_REAL_TIME_STREAM: {
                // 实时流，如麦克风输入，音频内容已经实时生成了
                uploader = new RealTimeUploader(inputStream, stat);
                break;
            }

            case MODE_SIMULATE_REAL_TIME_STREAM: {
                // 非实时流转为实时流
                RealTimeAudioFeeder feeder = new RealTimeAudioFeeder(inputStream);

                // 实时流上传
                uploader = new RealTimeUploader(feeder.getRealTimeInputStream(), stat);

                // 开新线程模拟音频输入
                new Thread(() -> {
                    try {
                        // 开始模拟实时流
                        feeder.startFeed();
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "sleep error", e);
                        throw new RuntimeException(e);
                    }
                }).start();
                break;
            }

            default:
                throw new RuntimeException("mode not implemented " + mode);

        }

        listener = new MyWebSocketListener(uploader, downloader);
        // STEP 1. 连接
        client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }

    public boolean isClosed() {
        return listener.isClosed();
    }
}
