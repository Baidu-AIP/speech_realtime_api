package com.baidu.ai.speech.realtime.android;

import android.os.Bundle;

import com.baidu.ai.speech.realtime.MiniMain;
import com.baidu.ai.speech.realtime.R;
import com.baidu.ai.speech.realtime.full.connection.Runner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UI界面启动实时语音识别接口
 * <p>
 * 鉴权信息请修改Const类
 */
public class MainActivity extends AbstractUIActivity {

    private final static int MINI_DEMO_MODE = 100;

    // ============ 以下参数可以修改 =============
    // 鉴权信息请修改Const类
    /**
     * 默认的录音pcm文件，在assets目录下
     */
    private final static String ASSET_PCM_FILENAME = "16k-0.pcm";

    /**
     * 日志级别
     * 较少的调试信息请使Level.INFO,
     * 更多使用 Level.ALL
     */
    private final static Level LOG_LEVEL = Level.INFO;

    /**
     * 默认的识别模式，
     * MINI_DEMO_MODE MiniMain 精简版，输入文件流
     * Runner.MODE_FILE_STREAM 完整版本，输入文件流
     * Runner.MODE_REAL_TIME_STREAM 完整版本，输入麦克风流
     * Runner.MODE_SIMULATE_REAL_TIME_STREAM 完整版本，输入文件流模拟实时流
     */
    private final static int DEFAULT_MODE = MINI_DEMO_MODE;

    // ============== 以下参数请勿修改 ================

    private volatile boolean isRunning = false;
    private InputStream is = null;

    private volatile MiniMain miniRunner;
    private volatile Runner fullRunner;


    private static Logger logger = Logger.getLogger("MainActivity");

    {
        mode = DEFAULT_MODE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // java.util.logging 转为 TextView里的文字

        LoggerUtil.addAndroidHandler(handler, LOG_LEVEL);
        logger.fine("start android demo");
        initButtons();
    }


    private void initButtons() {
        button = findViewById(R.id.btn_control);
        button.setOnClickListener((v) ->
            new Thread(() -> {
                // IO 操作都在新线程
                try {
                    if (isRunning) {
                        logger.info("点击停止");
                        close(false);
                    } else {
                        runOnUiThread(()->{
                            txtLog.setText("");
                            settingButton.setEnabled(false);
                            button.setText("停止");
                        });

                        start();
                        pollCheckStop();

                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getClass().getSimpleName() + ":" + e.getMessage(), e);
                }
            }).start()
        );
    }

    /**
     * 开始识别
     *
     * @throws IOException Assets 文件异常
     */
    private void start() throws IOException {
        logger.info("try to start " + mode);
        isRunning = true;
        if (mode == MINI_DEMO_MODE || mode == Runner.MODE_FILE_STREAM
                || mode == Runner.MODE_SIMULATE_REAL_TIME_STREAM) {
            is = getAssets().open(ASSET_PCM_FILENAME);
            // pcm 文件流
        } else if (mode == Runner.MODE_REAL_TIME_STREAM) {
            is = MyMicrophoneInputStream.getInstance();
            // 麦克风
        }
        if (mode == MINI_DEMO_MODE) {
            miniRunner = new MiniMain(is); // 精简版
            miniRunner.run();
        } else {
            fullRunner = new Runner(is, mode); // 完整版
            fullRunner.run();
        }
    }

    /**
     * 轮询检测websocket是否关闭
     */
    private void pollCheckStop() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if ((miniRunner != null && miniRunner.isClosed()) ||
                        (fullRunner != null && fullRunner.isClosed())) {
                    logger.info("switch to start 开始");
                    isRunning = false;
                    runOnUiThread(() -> {
                        settingButton.setEnabled(true);
                    });
                    button.setText("开始");
                }
                if (!isRunning) {
                    cancel();
                    timer.cancel();
                    close(true);
                }
            }
        };
        timer.schedule(timerTask, 500, 500);
    }

    /**
     * 流程：关闭InputStream-> uploader 结束-> websocket 关闭-> activity 里UI及参数重置
     * 关闭inputStream
     *
     * @param isRemoveRunners 是否设置为null
     */
    private void close(boolean isRemoveRunners) {
        logger.info("try to close");
        try {
            is.close();
        } catch (IOException | RuntimeException e) {
            logger.log(Level.SEVERE, e.getClass().getSimpleName() + ":" + e.getMessage(), e);
        } finally {
            if (isRemoveRunners) {
                is = null;
                miniRunner = null;
                fullRunner = null;
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

}
