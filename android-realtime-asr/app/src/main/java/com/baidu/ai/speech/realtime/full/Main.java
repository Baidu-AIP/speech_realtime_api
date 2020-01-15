package com.baidu.ai.speech.realtime.full;


import com.baidu.ai.speech.realtime.full.connection.Runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static final Level LOG_LEVEL = Level.ALL;

    public static final String FILENAME = "16k-0.pcm";

    /**
     * MODE_FILE_STREAM为非实时流，如文件流，数据已经完整地在流中
     * MODE_SIMULATE_REAL_TIME_STREAM 为非实时流，用来生成模拟实时流
     */
    private static final int MODE = Runner.MODE_FILE_STREAM;  // 或 MODE_SIMULATE_REAL_TIME_STREAM
    private static Logger logger = Logger.getLogger("Main");

    public static void main(String[] args) {
        logger.getParent().setLevel(LOG_LEVEL); // 设置级别 Level.INFO Level.FINER
        String filename = FILENAME;
        if (args.length >= 1) {
            filename = args[0];
        }
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT.%1$tL] [%4$-7s][%3$s] %5$s%6$s%n");
        Locale.setDefault(Locale.ENGLISH);
        File file = new File(filename);
        logger.info("begin demo, will read " + file.getAbsolutePath());
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            logger.info("file total size: " + inputStream.available());
            (new Runner(inputStream, MODE)).run();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
