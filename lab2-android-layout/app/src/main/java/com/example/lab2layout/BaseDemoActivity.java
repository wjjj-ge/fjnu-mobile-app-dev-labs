package com.example.lab2layout;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 五个布局演示页面共用的页头：返回键 + 标题 + 副标题。
 * 把重复的 findViewById 收在一处，各页面只管自己的内容区。
 */
public abstract class BaseDemoActivity extends AppCompatActivity {

    protected void bindHeader(int titleRes, String subtitle) {
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        View btnBack = findViewById(R.id.btnBack);

        tvTitle.setText(titleRes);
        tvSubtitle.setText(subtitle);
        btnBack.setOnClickListener(v -> finish());
    }
}
