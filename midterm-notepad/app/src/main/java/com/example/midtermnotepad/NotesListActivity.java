package com.example.midtermnotepad;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 笔记列表。两个基本要求都在这一页：卡片右下角显示「最后修改」的相对时间，
 * 顶部 SearchView 按标题或正文模糊查询并高亮命中片段。
 */
public class NotesListActivity extends AppCompatActivity implements NoteAdapter.Listener {

    private static final int FILTER_ALL = -2;

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private MaterialToolbar toolbar;
    private RecyclerView listView;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptyHint;
    private ChipGroup chipGroup;

    private NoteAdapter adapter;
    private int filterCategory = FILTER_ALL;
    private int sort = NoteStore.SORT_MODIFIED;
    private String keyword = "";
    private long reloadToken = 0;
    private int appliedTheme = Prefs.THEME_LIGHT;

    private final ContentObserver observer = new ContentObserver(main) {
        @Override
        public void onChange(boolean selfChange) {
            reload();
        }
    };

    private final ActivityResultLauncher<String[]> openDocument =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    doRestore(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UI.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);

        toolbar = findViewById(R.id.toolbar);
        listView = findViewById(R.id.list);
        emptyState = findViewById(R.id.emptyState);
        emptyTitle = findViewById(R.id.emptyTitle);
        emptyHint = findViewById(R.id.emptyHint);
        chipGroup = findViewById(R.id.chipGroup);
        ExtendedFloatingActionButton fab = findViewById(R.id.fab);

        UI.edgeToEdge(this, toolbar, findViewById(R.id.root));
        setSupportActionBar(toolbar);

        sort = Prefs.defaultSort(this);
        appliedTheme = Prefs.theme(this);
        adapter = new NoteAdapter(this, Prefs.showSnippet(this));
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipLife) {
                filterCategory = NotePad.CATEGORY_LIFE;
            } else if (id == R.id.chipStudy) {
                filterCategory = NotePad.CATEGORY_STUDY;
            } else if (id == R.id.chipWork) {
                filterCategory = NotePad.CATEGORY_WORK;
            } else if (id == R.id.chipIdea) {
                filterCategory = NotePad.CATEGORY_IDEA;
            } else {
                filterCategory = FILTER_ALL;
            }
            reload();
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteEditorActivity.class);
            startActivity(intent);
        });

        getContentResolver().registerContentObserver(
                NotePad.Notes.CONTENT_URI, true, observer);
        io.execute(() -> {
            Seed.seedIfEmpty(this);
            main.post(this::reload);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Prefs.theme(this) != appliedTheme) {
            // 主题改了要整页重建，否则底色换了、控件仍停在旧主题的配色上
            recreate();
            return;
        }
        sort = Prefs.defaultSort(this);
        // 从编辑器返回时「显示摘要」开关可能已改变，重建适配器成本很低
        adapter = new NoteAdapter(this, Prefs.showSnippet(this));
        listView.setAdapter(adapter);
        reload();
    }

    @Override
    protected void onDestroy() {
        getContentResolver().unregisterContentObserver(observer);
        io.shutdownNow();
        super.onDestroy();
    }

    // ------------------------------------------------------------------ 查询

    private void reload() {
        final long token = ++reloadToken;
        final int category = filterCategory;
        final int sortMode = sort;
        final String kw = keyword;

        io.execute(() -> {
            List<NoteItem> notes = NoteStore.query(this, kw, category, sortMode);
            int total = NoteStore.count(this);
            main.post(() -> {
                if (token != reloadToken || isFinishing()) {
                    return;
                }
                adapter.submit(notes, kw, getResources().getColor(
                        Prefs.theme(this) == Prefs.THEME_DARK ? R.color.highlight : R.color.highlight, null));
                boolean searching = !kw.isEmpty() || category != FILTER_ALL;
                if (notes.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    emptyTitle.setText(searching ? R.string.empty_search_title : R.string.empty_title);
                    emptyHint.setText(searching ? R.string.empty_search_hint : R.string.empty_hint);
                } else {
                    emptyState.setVisibility(View.GONE);
                }
                toolbar.setSubtitle(notes.isEmpty() ? null : (searching
                        ? ("筛选 " + notes.size() + " 条 · 共 " + total + " 条")
                        : ("共 " + total + " 条笔记")));
            });
        });
    }

    private void scheduleReload() {
        main.removeCallbacksAndMessages(null);
        main.postDelayed(this::reload, 220);
    }

    // ------------------------------------------------------------------ 菜单

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setQueryHint(getString(R.string.search_hint));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                keyword = query == null ? "" : query;
                reload();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                keyword = newText == null ? "" : newText;
                scheduleReload();
                return true;
            }
        });
        search.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                search.clearFocus();
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 排序不再是子菜单，直接把当前排序写进菜单项标题，省掉「勾在哪一项」这个问题
        menu.findItem(R.id.action_sort)
                .setTitle(getString(R.string.menu_sort_with, sortLabel(sort)));
        boolean empty = adapter.size() == 0;
        menu.findItem(R.id.action_export).setEnabled(!empty);
        menu.findItem(R.id.action_backup).setEnabled(!empty);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_export) {
            doExport();
            return true;
        } else if (id == R.id.action_backup) {
            doBackup();
            return true;
        } else if (id == R.id.action_restore) {
            openDocument.launch(new String[]{"application/json"});
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 排序用单选对话框，而不是 PopupMenu 的子菜单：
     * 子菜单没有「返回上一级」，选完想退出去只能把整份菜单重开一遍；
     * 对话框勾完即时生效并关闭，点「取消」或返回键也能直接退出。
     */
    private void showSortDialog() {
        final int[] modes = {NoteStore.SORT_MODIFIED, NoteStore.SORT_CREATED, NoteStore.SORT_TITLE};
        String[] labels = new String[modes.length];
        int checked = 0;
        for (int i = 0; i < modes.length; i++) {
            labels[i] = sortLabel(modes[i]);
            if (modes[i] == sort) {
                checked = i;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_sort)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (sort != modes[which]) {
                        sort = modes[which];
                        reload();
                        invalidateOptionsMenu();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private String sortLabel(int mode) {
        if (mode == NoteStore.SORT_CREATED) {
            return getString(R.string.sort_created);
        }
        if (mode == NoteStore.SORT_TITLE) {
            return getString(R.string.sort_title);
        }
        return getString(R.string.sort_modified);
    }

    // ------------------------------------------------------------------ 列表交互

    @Override
    public void onClick(NoteItem note) {
        Intent intent = new Intent(this, NoteEditorActivity.class);
        intent.putExtra(NoteEditorActivity.EXTRA_NOTE_ID, note.id);
        startActivity(intent);
    }

    @Override
    public void onLongClick(NoteItem note, View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, "打开").setIcon(android.R.drawable.ic_menu_edit);
        menu.getMenu().add(0, 2, 1, "复制正文").setIcon(android.R.drawable.ic_menu_set_as);
        menu.getMenu().add(0, 3, 2, "删除").setIcon(android.R.drawable.ic_menu_delete);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    onClick(note);
                    return true;
                case 2:
                    ClipboardManager clipboard =
                            (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("笔记",
                            note.displayTitle() + "\n" + (note.note == null ? "" : note.note)));
                    toast(getString(R.string.toast_copied));
                    return true;
                case 3:
                    confirmDelete(note);
                    return true;
                default:
                    return false;
            }
        });
        menu.show();
    }

    private void confirmDelete(NoteItem note) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dlg_delete_title)
                .setMessage(R.string.dlg_delete_msg)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (d, w) -> io.execute(() -> {
                    NoteStore.delete(this, note.id);
                    main.post(() -> toast(getString(R.string.toast_deleted)));
                }))
                .show();
    }

    // ------------------------------------------------------------------ 导出 / 备份 / 恢复

    private void doExport() {
        final String kw = keyword;
        final int category = filterCategory;
        final int sortMode = sort;
        io.execute(() -> {
            List<NoteItem> notes = NoteStore.query(this, kw, category, sortMode);
            try {
                String name = ExportBackup.exportMarkdown(this, notes);
                main.post(() -> toast(getString(R.string.export_done, notes.size())));
            } catch (Exception e) {
                main.post(() -> toast(getString(R.string.io_failed, e.getMessage())));
            }
        });
    }

    private void doBackup() {
        io.execute(() -> {
            List<NoteItem> notes = NoteStore.query(this, null, FILTER_ALL, sort);
            try {
                String name = ExportBackup.backupJson(this, notes);
                main.post(() -> toast(getString(R.string.backup_done, notes.size())));
            } catch (Exception e) {
                main.post(() -> toast(getString(R.string.io_failed, e.getMessage())));
            }
        });
    }

    private void doRestore(Uri uri) {
        io.execute(() -> {
            try {
                int count = ExportBackup.restoreJson(this, uri);
                main.post(() -> toast(getString(R.string.restore_done, count)));
            } catch (Exception e) {
                main.post(() -> toast(getString(R.string.restore_failed, e.getMessage())));
            }
        });
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
