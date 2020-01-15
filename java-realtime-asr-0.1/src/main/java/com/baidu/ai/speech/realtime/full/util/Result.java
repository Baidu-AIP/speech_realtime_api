package com.baidu.ai.speech.realtime.full.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 接收结果的解析类
 */
public class Result {

    /**
     * 一句话的临时结果type
     */
    public static final String TYPE_MID_TEXT = "MID_TEXT";

    /**
     * 一句话的最终结果type
     */
    public static final String TYPE_FIN_TEXT = "FIN_TEXT";

    /**
     * 心跳帧type，为了保持客户端连接，无业务含义
     */
    public static final String TYPE_HEARTBEAT = "HEARTBEAT";

    /**
     * 错误码。0 表示正确
     */
    private int errNo;

    /**
     * errNo不为0时的报错信息
     */
    private String errMsg;

    /**
     * 类型，TYPE_MID_TEXT TYPE_FIN_TEXT 或者 TYPE_HEARTBEAT
     */
    private String type;

    /**
     * 识别结果结果TYPE_MID_TEXT和TYPE_FIN_TEXT
     */
    private String result;

    /**
     * 一句话的开始时间，TYPE_FIN_TEXT有
     */
    private long startTime = -1;

    /**
     * 一句话的结束时间，TYPE_FIN_TEXT有
     */
    private long endTime = -1;

    /**
     * 服务端排查日志用
     */
    private String sn;

    /**
     * 服务端排查日志用
     */
    private long logId;

    /**
     * 原始json
     */
    private String orignalJsonStr;

    /**
     * 收到结果的时间点
     */
    private long receiveTime;

    public Result(String jsonStr) throws JSONException {
        // 接收时间用于统计
        receiveTime = System.currentTimeMillis();
        this.orignalJsonStr = jsonStr;
        JSONObject json = new JSONObject(jsonStr);

        // 这个帧的业务类型
        type = json.getString("type");
        if (isHeartBeat()) {
            return;
        }
        // 是否是错误
        errNo = json.getInt("err_no");
        errMsg = json.getString("err_msg");

        // 识别结果和类型
        type = json.getString("type");

        // 识别结果
        result = json.optString("result", null);

        // 一句话的开始和结束时间
        if (json.has("start_time")) {
            startTime = json.getLong("start_time");
            endTime = json.getLong("end_time");
        }

        // 日志
        logId = json.getLong("log_id");
        sn = json.optString("sn", null);
    }

    /**
     * 是否是心跳帧，心跳帧无实际意义
     *
     * @return
     */
    public boolean isHeartBeat() {
        return TYPE_HEARTBEAT.equals(type);
    }

    /**
     * 是否是结束帧
     *
     * @return
     */
    public boolean isFin() {
        return TYPE_FIN_TEXT.equals(type);
    }

    public boolean isError() {
        return errNo != 0;
    }

    public int getErrNo() {
        return errNo;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public String getType() {
        return type;
    }

    public String getResult() {
        return result;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getSn() {
        return sn;
    }

    public long getLogId() {
        return logId;
    }

    public String getOrignalJsonStr() {
        return orignalJsonStr;
    }

    public long getReceiveTime() {
        return receiveTime;
    }
}
