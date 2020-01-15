package com.baidu.ai.speech.realtime.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.ai.speech.realtime.R;
import com.baidu.ai.speech.realtime.full.connection.Runner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 这里都是UI界面
 */
public abstract class AbstractUIActivity extends AppCompatActivity {
    protected Button button;
    protected Button settingButton;
    protected Handler handler;
    protected TextView txtLog;

    protected int mode;
    private static Logger logger = Logger.getLogger("AbstractUIActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStrictMode();
        setContentView(R.layout.common_mini);
        initUi();
        initPermission();
    }

    private void initUi() {
        txtLog = findViewById(R.id.txtLog);
        settingButton = findViewById(R.id.btn_setting);
        settingButton.setOnClickListener((View v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle("识别空闲时切换");
            final Map<String, Integer> map = new LinkedHashMap<>(4);
            map.put("输入assets下pcm文件流, 精简版", 100);
            map.put("输入assets下pcm文件流", Runner.MODE_FILE_STREAM);
            map.put("输入麦克风实时流", Runner.MODE_REAL_TIME_STREAM);
            map.put("输入assets下录音文件流，模拟实时流", Runner.MODE_SIMULATE_REAL_TIME_STREAM);
            final String[] keysTemp = new String[4];
            final String[] keys = map.keySet().toArray(keysTemp);
            builder.setItems(keys, (dialog, which) -> {
                mode = map.get(keys[which]);
                logger.info("切换mode:" + mode + ":" + keys[which]);
            });
            builder.show();
        });

        ScrollView sv = findViewById(R.id.scroll_view);
        handler = new MyHandler(txtLog, sv);


    }


    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
        };
        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

    /**
     * 严格模式
     */
    private void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

    }


    private static class MyHandler extends Handler {
        private int textViewLines = 0;

        private TextView txtLog;

        private ScrollView sv;

        public MyHandler(TextView txtLog, ScrollView sv) {
            super();
            this.txtLog = txtLog;
            this.sv = sv;
        }

        /*
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            if (msg.obj != null) {
                textViewLines++;
                if (textViewLines > 200) {
                    textViewLines = 0;
                    txtLog.setText("");
                }
                txtLog.append(msg.obj.toString() + "\n");

            }
        }
    }
}
