package com.example.lab2layout;

import android.os.Bundle;

/**
 * 线性布局：4 行 × 4 格。
 * 行高用 layout_weight 均分，格宽用 weight 按课件实测比例分配
 * （第 1、2 行 21:33:24:22，第 3 行 24:25:27:25，第 4 行 21:32:24:23）。
 */
public class LinearLayoutActivity extends BaseDemoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear);
        bindHeader(R.string.title_linear,
                "LinearLayout 嵌套：外层 4 行等高，内层每行 4 格按 layout_weight 分宽");
    }
}
