package com.baidu.ai.speech.realtime.android;

import android.os.Message;
import android.util.Log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerUtil {

    public static void addAndroidHandler(android.os.Handler handler) {
        addAndroidHandler(handler, Level.FINE);
    }

    /**
     * Level.ALL 打开所有sdk的日志
     *
     * @param level
     */
    public static void addAndroidHandler(android.os.Handler handler, Level level) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(level);
        rootLogger.addHandler(new AndroidLoggingHandler(handler));
    }


    private static class AndroidLoggingHandler extends Handler {

        private android.os.Handler handler;

        public AndroidLoggingHandler(android.os.Handler handler) {
            this.handler = handler;
        }

        @Override
        public void publish(LogRecord record) {
            if (!super.isLoggable(record)) {
                return;
            }

            String tag = record.getLoggerName();
            try {
                Level l = record.getLevel();
                if (l.intValue() < Level.INFO.intValue()) {
                    Log.println(getAndroidLevel(l), tag + "|" + l.getName(), record.getMessage());
                }
                Message message = handler.obtainMessage(0, l.intValue());
                message.obj = "[" + tag + "] " + record.getMessage() + "\n";
                handler.sendMessage(message);
            } catch (RuntimeException e) {
                Log.e("AndroidLoggingHandler", "Error logging message.", e);
            }
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
        }

        private int getAndroidLevel(Level level) {
            int value = level.intValue();

            if (value >= Level.SEVERE.intValue()) {
                return Log.ERROR;
            } else if (value >= Level.WARNING.intValue()) {
                return Log.WARN;
            } else {
                return Log.INFO; // 大多数机型只打印INFO以上级别的日志
            }
        }
    }
}
