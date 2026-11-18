package com.example.lab3components;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

/**
 * 第 3 题：用 XML 定义菜单。
 * menu_text.xml 里放了两个子菜单（字体大小、字体颜色）和一个普通菜单项，
 * 选择组用 checkableBehavior="single"，天然就是互斥的单选效果。
 * 因为主题是 NoActionBar，菜单挂在页头右侧的 ⋮ 上用 PopupMenu 弹出。
 */
public class MenuActivity extends BaseDemoActivity {

    private TextView tvTest;
    private TextView tvState;

    private String sizeLabel;
    private String colorLabel;
    private float fontSizeSp = 16;
    private int fontColor = 0xFF1F2226;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        bindHeader(R.string.nav_03_title, "res/menu + PopupMenu");

        tvTest = findViewById(R.id.tvTest);
        tvState = findViewById(R.id.tvState);
        sizeLabel = getString(R.string.size_medium);
        colorLabel = getString(R.string.color_black_name);
        updateState();

        View overflow = showOverflow();
        overflow.setOnClickListener(this::showMenu);
    }

    private void showMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_text, popup.getMenu());
        // 每次弹出前按当前状态恢复勾选：PopupMenu 每次都是新 inflate 的，
        // 上一轮 setChecked 的结果不会留在菜单里，不恢复就会出现「正文已经 20sp 红色、
        // 菜单却还勾着中号 / 黑色」的错位。
        syncChecked(popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.font_small) {
                setFontSize(10, R.string.size_small);
            } else if (id == R.id.font_medium) {
                setFontSize(16, R.string.size_medium);
            } else if (id == R.id.font_large) {
                setFontSize(20, R.string.size_large);
            } else if (id == R.id.color_red) {
                setFontColor(0xFFD32F2F, R.string.color_red_name);
            } else if (id == R.id.color_black) {
                setFontColor(0xFF1F2226, R.string.color_black_name);
            } else if (id == R.id.menu_normal) {
                Toast.makeText(this, R.string.toast_normal_item, Toast.LENGTH_SHORT).show();
                return true;
            }
            item.setChecked(true);
            return true;
        });
        popup.show();
    }

    /** 把菜单里的单选项对齐到当前状态。 */
    private void syncChecked(android.view.Menu menu) {
        if (fontSizeSp <= 10) {
            menu.findItem(R.id.font_small).setChecked(true);
        } else if (fontSizeSp <= 16) {
            menu.findItem(R.id.font_medium).setChecked(true);
        } else {
            menu.findItem(R.id.font_large).setChecked(true);
        }
        menu.findItem(fontColor == 0xFFD32F2F ? R.id.color_red : R.id.color_black).setChecked(true);
    }

    private void setFontSize(float sp, int labelRes) {
        fontSizeSp = sp;
        tvTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        sizeLabel = getString(labelRes);
        updateState();
    }

    private void setFontColor(int color, int labelRes) {
        fontColor = color;
        tvTest.setTextColor(color);
        colorLabel = getString(labelRes);
        updateState();
    }

    private void updateState() {
        tvState.setText(getString(R.string.state_fmt, sizeLabel, colorLabel));
        // 状态行跟着正文颜色走，红色档时上下文更直观
        tvState.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
    }
}
