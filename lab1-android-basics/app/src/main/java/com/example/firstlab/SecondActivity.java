package com.example.firstlab;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 实验1 的第二个 Activity：本身界面很简单，作用是把 MainActivity 推到后台，
 * 让 onPause() / onStop() 与返回时的 onRestart() 可被观察到。
 */
public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "Lab1Lifecycle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.second_root), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.i(TAG, "SecondActivity.onCreate()");

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "SecondActivity.onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "SecondActivity.onResume()");
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "SecondActivity.onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "SecondActivity.onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "SecondActivity.onDestroy()");
        super.onDestroy();
    }
}
