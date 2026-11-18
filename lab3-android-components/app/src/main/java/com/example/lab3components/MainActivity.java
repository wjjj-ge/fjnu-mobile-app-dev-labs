package com.example.lab3components;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 实验3 的目录页：五道组件题各占一张卡片。
 * 每张卡片标注这道题真正练的 API（SimpleAdapter / setView / res-menu / ActionMode / ViewHolder），
 * 截图与答辩时一眼能对上课程要求。
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bind(R.id.cardList, ListViewActivity.class);
        bind(R.id.cardDialog, DialogActivity.class);
        bind(R.id.cardMenu, MenuActivity.class);
        bind(R.id.cardActionMode, ActionModeActivity.class);
        bind(R.id.cardRecycler, RecyclerActivity.class);
    }

    private void bind(int viewId, Class<?> target) {
        View card = findViewById(viewId);
        card.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, target)));
    }
}
