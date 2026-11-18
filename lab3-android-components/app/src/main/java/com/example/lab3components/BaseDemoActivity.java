package com.example.lab3components;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 五道组件题共用的页头：返回键 + 标题 + 副标题，第 3 题额外把右上角 ⋮ 亮出来。
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

    /** 第 3 题专用：让页头右侧的 ⋮ 显示出来并返回它，供 PopupMenu 当锚点。 */
    protected View showOverflow() {
        View overflow = findViewById(R.id.btnOverflow);
        overflow.setVisibility(View.VISIBLE);
        return overflow;
    }
}
