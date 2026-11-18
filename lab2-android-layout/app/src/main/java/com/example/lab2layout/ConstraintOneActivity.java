package com.example.lab2layout;

import android.os.Bundle;

/**
 * 约束布局 1：计算器键盘。
 * 16 个按键用四条横向链（chain，spread + 等权重）排布，
 * 行与行之间只靠「上一行第一个键的底部」一条约束往下推，不再嵌套 LinearLayout。
 */
public class ConstraintOneActivity extends BaseDemoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constraint_one);
        bindHeader(R.string.title_constraint_one,
                "ConstraintLayout 链式约束：4×4 键盘全部平铺在同一个 ConstraintLayout 里");
    }
}
