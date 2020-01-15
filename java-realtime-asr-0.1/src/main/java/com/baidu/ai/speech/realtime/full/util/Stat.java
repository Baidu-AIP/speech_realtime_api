package com.baidu.ai.speech.realtime.full.util;


import com.baidu.ai.speech.realtime.Util;
import com.baidu.ai.speech.realtime.full.download.Result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Stat {

    private volatile long beforeConnectTime;

    private volatile long onOpenTime;

    private volatile long afterStartFrameTime;

    private volatile long afterFinishFrameTime;

    private volatile long lastResultRecieveTime;

    private List<Result> results;

    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");

    public Stat() {
        results = Collections.synchronizedList(new ArrayList<>());
    }

    public void updateBeforeConnectTime() {
        this.beforeConnectTime = System.currentTimeMillis();
    }

    public void updateOnOpenTime() {
        this.onOpenTime = System.currentTimeMillis();
    }

    public void updateAfterStartFrameTime() {
        this.afterStartFrameTime = System.currentTimeMillis();
    }

    public void updateAfterFinishFrameTime() {
        this.afterFinishFrameTime = System.currentTimeMillis();
    }

    public void addResult(Result result) {
        lastResultRecieveTime = System.currentTimeMillis();
        results.add(result);
    }

    public String toReportString() {
        StringBuilder sb = new StringBuilder("\n");
        sb.append(formatTime(beforeConnectTime, "准备发起websocket连接"));
        sb.append(formatTime(onOpenTime, "websocket 连接建立"));
        sb.append(formatTime(afterStartFrameTime, "第一个FRAME开始START帧发送完成"));
        sb.append(formatTime(afterFinishFrameTime, "最后一个FRAME结束FINISH帧发送完成"));
        sb.append(formatTime(lastResultRecieveTime, "最后一个句子的识别结果收到"));
        sb.append("==============\n");
        for (Result result : results) {
            sb.append(formatTime(result.getReceiveTime(), formatResult(result).toString()));
        }
        return sb.toString();
    }

    public static StringBuilder formatResult(Result result) {
        StringBuilder sb = new StringBuilder();
        if (result.getErrNo() == -3005) {
            sb.append("【噪音】");
        } else if (result.getErrNo() != 0) {
            sb.append("【出错】");
        } else if (result.getType().equals(Result.TYPE_FIN_TEXT)) {
            sb.append("【一句话最终结果】");
        } else if (result.getType().equals(Result.TYPE_MID_TEXT)) {
            sb.append("【一句话临时结果】");
        }
        if (result.getResult() != null) {
            sb.append(result.getResult());
        }
        if (result.getErrNo() != 0) {
            sb.append("[").append(result.getErrNo()).append(" | ").append(result.getErrMsg()).append("]");
        }
        if (result.getStartTime() >= 0) {
            long startTime = result.getStartTime();
            long endTime = result.getEndTime();
            sb.append("[时长： ").append(result.getStartTime()).append("ms 至 ").append(endTime).append("ms]");
            sb.append("[字节： ").append(Util.timeToBytes(startTime)).append(" 至 ")
                    .append(Util.timeToBytes(endTime)).append("]");
        }
        return sb;
    }

    private StringBuilder formatTime(long timeStamp, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(formatter.format(new Date(timeStamp))).append("] ");
        sb.append(message).append("\n");
        return sb;
    }
}
