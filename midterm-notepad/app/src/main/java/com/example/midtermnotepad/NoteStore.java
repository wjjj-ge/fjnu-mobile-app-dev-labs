package com.example.midtermnotepad;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/** 对 ContentProvider 的一层薄封装，所有方法都是同步的，调用方负责放到后台线程。 */
public class NoteStore {

    private static final String[] PROJECTION = {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_NOTE,
            NotePad.Notes.COLUMN_NAME_CREATED,
            NotePad.Notes.COLUMN_NAME_MODIFIED,
            NotePad.Notes.COLUMN_NAME_CATEGORY,
    };

    public static final int SORT_MODIFIED = 0;
    public static final int SORT_CREATED = 1;
    public static final int SORT_TITLE = 2;

    public static String sortOrder(int sort) {
        switch (sort) {
            case SORT_CREATED:
                return NotePad.Notes.COLUMN_NAME_CREATED + " DESC";
            case SORT_TITLE:
                return NotePad.Notes.COLUMN_NAME_TITLE + " COLLATE NOCASE ASC";
            default:
                return NotePad.Notes.COLUMN_NAME_MODIFIED + " DESC";
        }
    }

    /**
     * 查询笔记列表。
     *
     * @param keyword  标题或正文的模糊匹配关键词，null / 空串表示全部
     * @param category 分类过滤，{@link NotePad#CATEGORY_NONE}-1 之外的负值表示不限
     */
    public static List<NoteItem> query(Context context, String keyword, int category, int sort) {
        Uri uri = NotePad.Notes.CONTENT_URI;
        String selection = null;
        String[] args = null;
        List<String> parts = new ArrayList<>();

        if (category >= 0) {
            parts.add(NotePad.Notes.COLUMN_NAME_CATEGORY + "=" + category);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim().replace("'", "''") + "%";
            parts.add("(" + NotePad.Notes.COLUMN_NAME_TITLE + " LIKE '" + like + "' OR "
                    + NotePad.Notes.COLUMN_NAME_NOTE + " LIKE '" + like + "')");
        }
        if (!parts.isEmpty()) {
            selection = join(parts, " AND ");
        }

        List<NoteItem> result = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(uri, PROJECTION, selection, args, sortOrder(sort))) {
            if (cursor == null) {
                return result;
            }
            int idxId = cursor.getColumnIndexOrThrow(NotePad.Notes._ID);
            int idxTitle = cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_TITLE);
            int idxNote = cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_NOTE);
            int idxCreated = cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CREATED);
            int idxModified = cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_MODIFIED);
            int idxCategory = cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CATEGORY);
            while (cursor.moveToNext()) {
                result.add(new NoteItem(
                        cursor.getLong(idxId),
                        cursor.getString(idxTitle),
                        cursor.getString(idxNote),
                        cursor.getLong(idxCreated),
                        cursor.getLong(idxModified),
                        cursor.getInt(idxCategory)));
            }
        }
        return result;
    }

    public static int count(Context context) {
        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(NotePad.Notes.CONTENT_URI,
                PROJECTION, null, null, null)) {
            return cursor == null ? 0 : cursor.getCount();
        }
    }

    public static NoteItem get(Context context, long id) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, id);
        try (Cursor cursor = resolver.query(uri, PROJECTION, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
            return new NoteItem(
                    cursor.getLong(cursor.getColumnIndexOrThrow(NotePad.Notes._ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_NOTE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CREATED)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_MODIFIED)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CATEGORY)));
        }
    }

    /** 新建笔记，返回新 id，失败返回 -1。 */
    public static long insert(Context context, String title, String note, int category) {
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, note);
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, category);
        Uri uri = context.getContentResolver().insert(NotePad.Notes.CONTENT_URI, values);
        return uri == null ? -1 : ContentUris.parseId(uri);
    }

    public static void update(Context context, long id, String title, String note, int category) {
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, note);
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, category);
        Uri uri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, id);
        context.getContentResolver().update(uri, values, null, null);
    }

    public static void delete(Context context, long id) {
        Uri uri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, id);
        context.getContentResolver().delete(uri, null, null);
    }

    private static String join(List<String> parts, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append(sep);
            }
            sb.append(parts.get(i));
        }
        return sb.toString();
    }
}
