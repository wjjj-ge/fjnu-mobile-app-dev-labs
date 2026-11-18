package com.example.lab2layout;

import android.os.Bundle;

/**
 * 约束布局 2：太空订票首页。
 * 页签等权重、城市两块等宽、DEPART 贴底，全部靠约束和权重算出，
 * 没有任何一层写死高度之外的魔法数字。
 */
public class ConstraintTwoActivity extends BaseDemoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constraint_two);
        bindHeader(R.string.title_constraint_two,
                "ConstraintLayout 比例约束：页签等分、城市等宽、主视觉居中、按钮贴底");
    }
}
