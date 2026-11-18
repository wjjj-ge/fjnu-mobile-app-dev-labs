package com.example.lab2layout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 实验2 的目录页：四道布局题各占一张卡片，点哪张进哪题。
 * 每一张卡片上标了这道题真正练的东西（weight / stretchColumns / chain / ratio），
 * 这样截图和答辩时都能一眼对上课程要求。
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bind(R.id.cardLinear, LinearLayoutActivity.class);
        bind(R.id.cardTable, TableLayoutActivity.class);
        bind(R.id.cardConstraintOne, ConstraintOneActivity.class);
        bind(R.id.cardConstraintTwo, ConstraintTwoActivity.class);
        bind(R.id.cardCompose, TodoActivity.class);
    }

    private void bind(int viewId, Class<?> target) {
        View card = findViewById(viewId);
        card.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, target)));
    }
}
