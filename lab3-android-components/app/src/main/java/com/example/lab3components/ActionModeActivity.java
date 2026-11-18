package com.example.lab3components;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 第 4 题：ActionMode 上下文菜单。
 * ListView 用 CHOICE_MODE_MULTIPLE_MODAL，长按任何一行系统就会拉起 ActionMode，
 * 之后点行 = 勾选/取消；标题实时显示「N selected」，垃圾桶删除选中项。
 */
public class ActionModeActivity extends BaseDemoActivity {

    private final List<String> items = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actionmode);
        bindHeader(R.string.nav_04_title, "MultiChoiceMode / ActionMode");

        String[] seed = getResources().getStringArray(R.array.action_items);
        for (String s : seed) items.add(s);

        adapter = new ArrayAdapter<>(this, R.layout.item_actionmode, R.id.tvName, items);
        list = findViewById(R.id.listActions);
        list.setAdapter(adapter);

        list.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                mode.setTitle(getString(R.string.selected_fmt, list.getCheckedItemCount()));
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_action, menu);
                mode.setTitle(getString(R.string.selected_fmt, 0));
                // ActionMode 的操作栏是系统浅灰底、并且会盖住状态栏区域，
                // 而这里的状态栏图标默认是白色（因为平时页头是深navy），白字压浅底就看不清了。
                // 进多选态切成深色图标，退出时再切回来。
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                        .setAppearanceLightStatusBars(true);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_delete) {
                    int removed = deleteChecked();
                    Toast.makeText(ActionModeActivity.this,
                            getString(R.string.deleted_fmt, removed), Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // 退出时不留残留勾选，下次长按从 0 开始；状态栏图标也切回白色
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                        .setAppearanceLightStatusBars(false);
            }
        });
    }

    /** 从后往前删，避免删一个后面的下标就整体前移。 */
    private int deleteChecked() {
        int count = 0;
        for (int i = items.size() - 1; i >= 0; i--) {
            if (list.getCheckedItemPositions().get(i)) {
                items.remove(i);
                count++;
            }
        }
        adapter.notifyDataSetChanged();
        return count;
    }
}
