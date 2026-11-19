package com.example.midtermnotepad;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 导出 / 备份 / 恢复：
 * <ul>
 *   <li>导出：把当前列表里的笔记拼成一份 Markdown，写进「下载/NotePad」；</li>
 *   <li>备份：同样的数据写成 JSON，恢复时按原时间戳插回数据库。</li>
 * </ul>
 * API 29 起用 MediaStore 写入公共下载目录（不需要存储权限），更低版本退到应用专属目录。
 */
public final class ExportBackup {

    private static final String DIR = "NotePad";
    private static final SimpleDateFormat STAMP =
            new SimpleDateFormat("yyyyMMdd-HHmm", Locale.CHINA);
    private static final SimpleDateFormat READABLE =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    private ExportBackup() {
    }

    // ---------------------------------------------------------------- 导出 Markdown

    public static String exportMarkdown(Context context, List<NoteItem> notes) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# 记事本导出\n\n");
        sb.append("> 导出时间：").append(READABLE.format(new Date()))
                .append("　笔记数：").append(notes.size()).append("\n\n");

        for (NoteItem note : notes) {
            sb.append("## ").append(note.displayTitle()).append("\n\n");
            sb.append("- 分类：").append(UI.categoryLabel(note.category)).append("\n");
            sb.append("- 创建：").append(READABLE.format(new Date(note.created))).append("\n");
            sb.append("- 修改：").append(READABLE.format(new Date(note.modified))).append("\n\n");
            String body = note.note == null ? "" : note.note.trim();
            sb.append(body.isEmpty() ? "_（无正文）_" : body).append("\n\n---\n\n");
        }

        String name = "notes-" + STAMP.format(new Date()) + ".md";
        write(context, name, "text/markdown", sb.toString());
        return name;
    }

    // ---------------------------------------------------------------- 备份 JSON

    public static String backupJson(Context context, List<NoteItem> notes) throws IOException {
        JSONArray array = new JSONArray();
        for (NoteItem note : notes) {
            JSONObject o = new JSONObject();
            try {
                o.put("title", note.title == null ? "" : note.title);
                o.put("note", note.note == null ? "" : note.note);
                o.put("created", note.created);
                o.put("modified", note.modified);
                o.put("category", note.category);
                o.put("categoryName", UI.categoryLabel(note.category));
            } catch (Exception ignored) {
            }
            array.put(o);
        }
        JSONObject root = new JSONObject();
        try {
            root.put("app", "midterm-notepad");
            root.put("exportedAt", System.currentTimeMillis());
            root.put("count", notes.size());
            root.put("notes", array);
        } catch (Exception ignored) {
        }

        String name = "notepad-backup-" + STAMP.format(new Date()) + ".json";
        String json;
        try {
            json = root.toString(2);
        } catch (Exception e) {
            throw new IOException("生成备份内容失败");
        }
        write(context, name, "application/json", json);
        return name;
    }

    // ---------------------------------------------------------------- 恢复

    public static int restoreJson(Context context, Uri uri) throws IOException {
        String text;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new IOException("无法读取所选文件");
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = in.read(chunk)) > 0) {
                buffer.write(chunk, 0, read);
            }
            text = buffer.toString("UTF-8");
        }

        int inserted = 0;
        try {
            JSONObject root = new JSONObject(text);
            JSONArray array = root.optJSONArray("notes");
            if (array == null) {
                throw new IOException("不是本应用导出的备份文件");
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(NotePad.Notes.COLUMN_NAME_TITLE, o.optString("title", ""));
                values.put(NotePad.Notes.COLUMN_NAME_NOTE, o.optString("note", ""));
                values.put(NotePad.Notes.COLUMN_NAME_CATEGORY,
                        o.optInt("category", NotePad.CATEGORY_NONE));
                long created = o.optLong("created", System.currentTimeMillis());
                long modified = o.optLong("modified", created);
                String title = values.getAsString(NotePad.Notes.COLUMN_NAME_TITLE);
                if (exists(context, title, modified)) {
                    continue;   // 同标题 + 同修改时间的条目视为已存在，避免重复恢复
                }
                // 直接走 resolver，保留原时间戳；Provider 的 insert 会覆盖 modified，所以随后补写
                values.put(NotePad.Notes.COLUMN_NAME_CREATED, created);
                Uri newUri = context.getContentResolver()
                        .insert(NotePad.Notes.CONTENT_URI, values);
                if (newUri != null) {
                    ContentValues fix = new ContentValues();
                    fix.put(NotePad.Notes.COLUMN_NAME_CREATED, created);
                    fix.put(NotePad.Notes.COLUMN_NAME_MODIFIED, modified);
                    context.getContentResolver().update(newUri, fix, null, null);
                    inserted++;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage() == null ? "备份解析失败" : e.getMessage());
        }
        return inserted;
    }

    // ---------------------------------------------------------------- 写文件

    /** 备份里已有同样的笔记（标题相同、修改时间相同）就不再重复插入。 */
    private static boolean exists(Context context, String title, long modified) {
        String selection = NotePad.Notes.COLUMN_NAME_TITLE + "=?"
                + " AND " + NotePad.Notes.COLUMN_NAME_MODIFIED + "=" + modified;
        try (android.database.Cursor cursor = context.getContentResolver().query(
                NotePad.Notes.CONTENT_URI, new String[]{NotePad.Notes._ID},
                selection, new String[]{title == null ? "" : title}, null)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    private static void write(Context context, String displayName, String mime, String content)
            throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/" + DIR);
            Uri uri = context.getContentResolver()
                    .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                throw new IOException("创建下载文件失败");
            }
            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                if (out == null) {
                    throw new IOException("打开输出流失败");
                }
                out.write(bytes);
            }
            return;
        }

        // 旧版本：写进应用专属外部目录，免去存储权限
        File dir = new File(context.getExternalFilesDir(null), DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("无法创建目录");
        }
        File file = new File(dir, displayName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
        }
    }
}
