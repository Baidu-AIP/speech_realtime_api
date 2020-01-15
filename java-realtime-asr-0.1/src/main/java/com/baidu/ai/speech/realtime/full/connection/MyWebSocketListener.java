package com.baidu.ai.speech.realtime.full.connection;

import com.baidu.ai.speech.realtime.full.download.SimpleDownloader;
import com.baidu.ai.speech.realtime.full.upload.AbstractUploader;
import com.baidu.ai.speech.realtime.full.download.Result;
import com.baidu.ai.speech.realtime.full.util.Stat;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyWebSocketListener extends WebSocketListener {

    private static Logger logger = Logger.getLogger("MyWebSocketListener");

    private AbstractUploader uploader;

    private SimpleDownloader downloader;

    private AtomicBoolean isClosed;

    private Stat stat;

    /**
     * @param uploader   发送数据类，上传参数和音频内容
     * @param downloader 接收数据类，获取识别结果
     */
    public MyWebSocketListener(AbstractUploader uploader, SimpleDownloader downloader) {
        isClosed = new AtomicBoolean(false); // 是否
        stat = uploader.getStat(); //一些统计数据

        this.uploader = uploader;
        this.downloader = downloader;
        stat.updateBeforeConnectTime();
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        super.onOpen(webSocket, response);
        stat.updateOnOpenTime();

        // 这里千万别阻塞，包括这个类其它回调
        new Thread(() -> {
            try {
                uploader.execute(webSocket);
            } catch (JSONException e) {
                logger.log(Level.SEVERE, "upload " + e.getClass().getSimpleName(), e);
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        super.onMessage(webSocket, text);
        // 这里千万别阻塞，包括这个类其它回调
        Result result;
        try {
            // 将json解析为Result类
            result = new Result(text);
        } catch (JSONException e) {
            logger.log(Level.SEVERE, "receive json parse error: " + e.getMessage() + ":" + text, e);
            e.printStackTrace();
            return;
        }
        if (result.isHeartBeat()) {
            logger.finest("receive heartbeat: " + text.trim());
        } else {
            logger.info("receive text: " + text.trim());
        }
        if (result.isFin()) {
            stat.addResult(result);
        }
        downloader.onMessage(result);

    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosed(webSocket, code, reason);
        // 这里千万别阻塞，包括这个类其它回调
        logger.info("websocket closed: " + code + " | " + reason);
        logger.info(stat.toReportString());
        setClosed();
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosing(webSocket, code, reason);
        // 这里千万别阻塞，包括这个类其它回调
        logger.info("websocket event closing :" + code + " | " + reason);
        webSocket.close(1000, "");
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        // 这里千万别阻塞，包括这个类其它回调
        logger.log(Level.SEVERE, "websocket failure :" + t.getMessage(), t);
        setClosed();
    }

    private void setClosed() {
        isClosed.set(true);
        uploader.setClosed();
    }


    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        super.onMessage(webSocket, bytes);
        logger.severe("receive binary unexpected: " + bytes.size());
        // never happen
    }

    public boolean isClosed() {
        return isClosed.get();
    }
}
