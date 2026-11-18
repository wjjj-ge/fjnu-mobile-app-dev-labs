package com.example.firstlab;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 实验1 主界面：Android 运行环境自检 + Activity 生命周期观测。
 *
 * <p>设计意图：把“看不见的生命周期”变成屏幕上可读的流水。
 * 每个回调都会以「墙钟时间 + T+相对毫秒 + 回调名」追加一行，
 * 同时写入 Logcat（Tag: Lab1Lifecycle），便于现场演示与写实验报告。</p>
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Lab1Lifecycle";
    /** 日志最多保留的行数，防止长时间操作后 TextView 无限膨胀 */
    private static final int MAX_LOG_LINES = 40;

    private TextView tvDeviceInfo;
    private TextView tvStateInfo;
    private TextView tvLog;

    private final StringBuilder logBuffer = new StringBuilder();
    /** 首次记录日志时的 elapsedRealtime，作为 T+ 的相对零点 */
    private long baseElapsedMs = -1L;
    private int createCount = 1;
    private int tapCount = 0;
    private String lastEvent = "启动中";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // savedInstanceState 不为 null，说明是“配置变更后重建”，而不是首次创建
        if (savedInstanceState != null) {
            createCount = savedInstanceState.getInt("createCount", 0) + 1;
        } else {
            createCount = 1;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        applyWindowInsets();
        bindViews();
        renderDeviceInfo();
        renderStateInfo();

        appendLog("onCreate()  " + (savedInstanceState == null
                ? "首次创建，savedInstanceState = null"
                : "重建，savedInstanceState 非空，本次是第 " + createCount + " 次 onCreate"));
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        tvDeviceInfo = findViewById(R.id.tv_device_info);
        tvStateInfo = findViewById(R.id.tv_state_info);
        tvLog = findViewById(R.id.tv_log);

        Button btnSecond = findViewById(R.id.btn_second);
        Button btnRotate = findViewById(R.id.btn_rotate);
        Button btnCounter = findViewById(R.id.btn_counter);
        Button btnClear = findViewById(R.id.btn_clear);

        // 显式 Intent：目标组件名写死，系统直接按类名找到 SecondActivity
        btnSecond.setOnClickListener(v -> {
            appendLog("startActivity(SecondActivity)  显式 Intent");
            startActivity(new Intent(MainActivity.this, SecondActivity.class));
        });

        btnRotate.setOnClickListener(v -> toggleOrientation());

        btnCounter.setOnClickListener(v -> {
            tapCount++;
            appendLog("点击计数 +1 → " + tapCount);
        });

        btnClear.setOnClickListener(v -> {
            logBuffer.setLength(0);
            tvLog.setText("");
            appendLog("日志已清空");
        });
    }

    /** 读取 Build 与 Resources 中的真实运行时信息 */
    private void renderDeviceInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("厂商 / 品牌 : ").append(Build.MANUFACTURER).append(" / ").append(Build.BRAND).append('\n');
        sb.append("设备型号   : ").append(Build.MODEL).append('\n');
        sb.append("系统版本   : Android ").append(Build.VERSION.RELEASE)
                .append("  (API ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("CPU 架构   : ").append(Build.SUPPORTED_ABIS[0]).append('\n');

        DisplayMetrics dm = getResources().getDisplayMetrics();
        sb.append("屏幕像素   : ").append(dm.widthPixels).append(" × ").append(dm.heightPixels).append('\n');
        sb.append("屏幕密度   : ").append(dm.densityDpi).append(" dpi  (density ")
                .append(String.format(Locale.US, "%.2f", dm.density)).append(")\n");
        sb.append("最小宽度   : ").append(getResources().getConfiguration().smallestScreenWidthDp).append(" dp\n");
        sb.append("屏幕方向   : ").append(isLandscape() ? "横屏 landscape" : "竖屏 portrait").append('\n');
        sb.append("应用包名   : ").append(getPackageName()).append('\n');
        sb.append("进程 PID   : ").append(android.os.Process.myPid());

        tvDeviceInfo.setText(sb.toString());
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void renderStateInfo() {
        if (tvStateInfo == null) {
            return;
        }
        String text = "onCreate 执行次数 : " + createCount + "\n"
                + "点击计数        : " + tapCount + "\n"
                + "最近回调        : " + lastEvent;
        tvStateInfo.setText(text);
    }

    /** 切换屏幕方向，主动制造一次配置变更，用来观察销毁重建 */
    private void toggleOrientation() {
        int target = isLandscape()
                ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        appendLog("setRequestedOrientation()  即将触发配置变更");
        setRequestedOrientation(target);
    }

    /** 追加一行日志：墙钟时间 + 相对时间 + 事件名，同时输出到 Logcat */
    private void appendLog(String event) {
        lastEvent = event;
        Log.i(TAG, event);

        if (tvLog == null) {
            return;
        }
        if (baseElapsedMs < 0) {
            baseElapsedMs = SystemClock.elapsedRealtime();
        }
        long delta = SystemClock.elapsedRealtime() - baseElapsedMs;
        String wallClock = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(new Date());

        logBuffer.append(String.format(Locale.US, "%s  T+%05dms  %s%n", wallClock, delta, event));
        trimLog();
        tvLog.setText(logBuffer.toString());
        renderStateInfo();
    }

    private void trimLog() {
        int lineCount = 0;
        for (int i = 0; i < logBuffer.length(); i++) {
            if (logBuffer.charAt(i) == '\n') {
                lineCount++;
            }
        }
        while (lineCount > MAX_LOG_LINES) {
            int firstBreak = logBuffer.indexOf("\n");
            if (firstBreak < 0) {
                break;
            }
            logBuffer.delete(0, firstBreak + 1);
            lineCount--;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        appendLog("onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        appendLog("onResume()");
    }

    @Override
    protected void onPause() {
        // 先记录，再交给父类：onPause 之后界面可能已被遮挡
        appendLog("onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        appendLog("onStop()");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        appendLog("onRestart()  从后台返回，实例未重建");
    }

    @Override
    protected void onDestroy() {
        appendLog("onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("createCount", createCount);
        outState.putInt("tapCount", tapCount);
        appendLog("onSaveInstanceState()  写入 createCount=" + createCount + ", tapCount=" + tapCount);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tapCount = savedInstanceState.getInt("tapCount", 0);
        appendLog("onRestoreInstanceState()  恢复 tapCount=" + tapCount);
        renderStateInfo();
    }
}
