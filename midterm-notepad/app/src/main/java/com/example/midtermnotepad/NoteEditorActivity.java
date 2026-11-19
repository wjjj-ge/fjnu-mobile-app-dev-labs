package com.example.midtermnotepad;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 编辑器：标题 + 正文 + 分类，保存时写回 ContentProvider（修改时间由 Provider 统一打点）。 */
public class NoteEditorActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "note_id";

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private MaterialToolbar toolbar;
    private EditText etTitle;
    private EditText etContent;
    private TextView tvMeta;
    private ChipGroup chipCategory;

    private long noteId = -1;
    private long created;
    private int category = NotePad.CATEGORY_NONE;
    private boolean dirty;
    private boolean loaded;
    private String screenTitle = "新建笔记";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UI.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        tvMeta = findViewById(R.id.tvMeta);
        chipCategory = findViewById(R.id.chipCategory);

        UI.edgeToEdge(this, toolbar, findViewById(R.id.root));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> {
            saveIfNeeded();
            finish();
        });

        etContent.setTextSize(Prefs.fontSize(this));

        noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);

        chipCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                category = NotePad.CATEGORY_NONE;
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipCatLife) {
                category = NotePad.CATEGORY_LIFE;
            } else if (id == R.id.chipCatStudy) {
                category = NotePad.CATEGORY_STUDY;
            } else if (id == R.id.chipCatWork) {
                category = NotePad.CATEGORY_WORK;
            } else if (id == R.id.chipCatIdea) {
                category = NotePad.CATEGORY_IDEA;
            } else {
                category = NotePad.CATEGORY_NONE;
            }
            dirty = true;
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (loaded) {
                    dirty = true;
                    updateMeta(System.currentTimeMillis());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        etTitle.addTextChangedListener(watcher);
        etContent.addTextChangedListener(watcher);

        if (noteId > 0) {
            screenTitle = "编辑笔记";
            load();
        } else {
            screenTitle = "新建笔记";
            created = System.currentTimeMillis();
            loaded = true;
            checkChip(NotePad.CATEGORY_NONE);
            updateMeta(created);
            etTitle.requestFocus();
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(screenTitle);
        }
    }

    private void load() {
        io.execute(() -> {
            NoteItem note = NoteStore.get(this, noteId);
            main.post(() -> {
                if (note == null || isFinishing()) {
                    finish();
                    return;
                }
                created = note.created;
                category = note.category;
                etTitle.setText(note.title);
                etContent.setText(note.note);
                etContent.setSelection(etContent.getText().length());
                checkChip(category);
                loaded = true;
                dirty = false;
                updateMeta(note.modified);
            });
        });
    }

    private void checkChip(int category) {
        int id;
        switch (category) {
            case NotePad.CATEGORY_LIFE:
                id = R.id.chipCatLife;
                break;
            case NotePad.CATEGORY_STUDY:
                id = R.id.chipCatStudy;
                break;
            case NotePad.CATEGORY_WORK:
                id = R.id.chipCatWork;
                break;
            case NotePad.CATEGORY_IDEA:
                id = R.id.chipCatIdea;
                break;
            default:
                id = R.id.chipCatNone;
                break;
        }
        Chip chip = chipCategory.findViewById(id);
        if (chip != null) {
            chip.setChecked(true);
        }
    }

    private void updateMeta(long modified) {
        int words = etContent.getText().toString().replaceAll("\\s", "").length();
        tvMeta.setText(getString(R.string.editor_meta,
                TimeUtil.absolute(created), TimeUtil.absolute(modified), words));
    }

    private void saveIfNeeded() {
        if (!loaded) {
            return;
        }
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString();
        if (title.isEmpty() && content.trim().isEmpty()) {
            if (noteId > 0) {
                NoteStore.delete(this, noteId);
            }
            return;
        }
        if (!dirty && noteId > 0) {
            return;
        }
        if (noteId > 0) {
            NoteStore.update(this, noteId, title, content, category);
        } else {
            noteId = NoteStore.insert(this, title, content, category);
        }
        dirty = false;
    }

    @Override
    public void onBackPressed() {
        saveIfNeeded();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveIfNeeded();
    }

    @Override
    protected void onDestroy() {
        io.shutdownNow();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveIfNeeded();
            Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dlg_delete_title)
                .setMessage(R.string.dlg_delete_msg)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    io.execute(() -> {
                        if (noteId > 0) {
                            NoteStore.delete(this, noteId);
                        }
                        main.post(() -> {
                            dirty = false;
                            loaded = false;
                            Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .show();
    }
}

