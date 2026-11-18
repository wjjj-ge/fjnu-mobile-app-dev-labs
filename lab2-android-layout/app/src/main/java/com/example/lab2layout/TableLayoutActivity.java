package com.example.lab2layout;

import android.os.Bundle;

/**
 * 表格布局：两列菜单表。
 * stretchColumns="1" 让第二列吃掉剩余宽度，快捷键才能靠右贴边；
 * 分组之间用 layout_span="2" 的 1dp 细线跨列分隔。
 */
public class TableLayoutActivity extends BaseDemoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        bindHeader(R.string.title_table,
                "TableLayout：TableRow 即行，stretchColumns 决定哪列拉伸，layout_span 实现跨列分隔线");
    }
}
