package com.example.midtermnotepad;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

/** 偏好设置：主题、编辑器字号、默认排序、列表摘要开关。改主题后返回列表会自动重建。 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UI.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        UI.edgeToEdge(this, toolbar, findViewById(R.id.root));
        toolbar.setNavigationOnClickListener(v -> finish());

        RadioGroup rgTheme = findViewById(R.id.rgTheme);
        int theme = Prefs.theme(this);
        rgTheme.check(theme == Prefs.THEME_DARK ? R.id.rbDark
                : theme == Prefs.THEME_SEPIA ? R.id.rbSepia : R.id.rbLight);
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int next = checkedId == R.id.rbDark ? Prefs.THEME_DARK
                    : checkedId == R.id.rbSepia ? Prefs.THEME_SEPIA : Prefs.THEME_LIGHT;
            if (next != Prefs.theme(this)) {
                Prefs.setTheme(this, next);
                // 主题要重建立即生效：重启当前页并让列表在下一次 onResume 重新读偏好
                recreate();
            }
        });

        TextView fontLabel = findViewById(R.id.tvFontLabel);
        SeekBar seek = findViewById(R.id.seekFont);
        seek.setMax(12);                       // 12..24 sp
        seek.setProgress(Prefs.fontSize(this) - 12);
        fontLabel.setText(getString(R.string.settings_font_size) + "：" + Prefs.fontSize(this) + "sp");
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = 12 + progress;
                fontLabel.setText(getString(R.string.settings_font_size) + "：" + size + "sp");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.setFontSize(SettingsActivity.this, 12 + seekBar.getProgress());
            }
        });

        RadioGroup rgSort = findViewById(R.id.rgSort);
        int sort = Prefs.defaultSort(this);
        rgSort.check(sort == NoteStore.SORT_CREATED ? R.id.rbSortCreated
                : sort == NoteStore.SORT_TITLE ? R.id.rbSortTitle : R.id.rbSortModified);
        rgSort.setOnCheckedChangeListener((group, checkedId) -> Prefs.setDefaultSort(this,
                checkedId == R.id.rbSortCreated ? NoteStore.SORT_CREATED
                        : checkedId == R.id.rbSortTitle ? NoteStore.SORT_TITLE
                        : NoteStore.SORT_MODIFIED));

        MaterialSwitch snippet = findViewById(R.id.swSnippet);
        snippet.setChecked(Prefs.showSnippet(this));
        snippet.setOnCheckedChangeListener((button, checked) ->
                Prefs.setShowSnippet(this, checked));
    }
}
