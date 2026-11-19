package com.example.midtermnotepad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * NotePad 的 ContentProvider：把 {@link NotePad.Notes} 暴露成 content:// URI，
 * 增删改之后统一 {@code notifyChange}，列表页靠 ContentObserver 自动刷新。
 */
public class NotePadProvider extends android.content.ContentProvider {

    private static final String DB_NAME = "notepad.db";
    private static final String TABLE = "notes";
    private static final int DB_VERSION = 2;

    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;

    private DbHelper helper;

    @Override
    public boolean onCreate() {
        helper = new DbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (match(uri)) {
            case NOTES:
                return NotePad.Notes.CONTENT_TYPE;
            case NOTE_ID:
                return NotePad.Notes.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("未知 URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String order = sortOrder == null ? NotePad.Notes.DEFAULT_SORT_ORDER : sortOrder;

        Cursor cursor;
        switch (match(uri)) {
            case NOTES:
                cursor = db.query(TABLE, projection, selection, selectionArgs, null, null, order);
                break;
            case NOTE_ID:
                cursor = db.query(TABLE, projection, withId(selection, uri),
                        withIdArgs(selectionArgs, uri), null, null, order);
                break;
            default:
                throw new IllegalArgumentException("未知 URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (match(uri) != NOTES) {
            throw new IllegalArgumentException("只能向 " + NotePad.Notes.CONTENT_URI + " 插入");
        }
        ContentValues v = values == null ? new ContentValues() : new ContentValues(values);
        long now = System.currentTimeMillis();
        if (!v.containsKey(NotePad.Notes.COLUMN_NAME_CREATED)) {
            v.put(NotePad.Notes.COLUMN_NAME_CREATED, now);
        }
        if (!v.containsKey(NotePad.Notes.COLUMN_NAME_MODIFIED)) {
            v.put(NotePad.Notes.COLUMN_NAME_MODIFIED, now);
        }
        if (!v.containsKey(NotePad.Notes.COLUMN_NAME_TITLE)) {
            v.put(NotePad.Notes.COLUMN_NAME_TITLE, "");
        }
        if (!v.containsKey(NotePad.Notes.COLUMN_NAME_CATEGORY)) {
            v.put(NotePad.Notes.COLUMN_NAME_CATEGORY, NotePad.CATEGORY_NONE);
        }

        long id = helper.getWritableDatabase().insert(TABLE, null, v);
        if (id <= 0) {
            return null;
        }
        Uri item = Uri.withAppendedPath(NotePad.Notes.CONTENT_URI, String.valueOf(id));
        getContext().getContentResolver().notifyChange(NotePad.Notes.CONTENT_URI, null);
        return item;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        ContentValues v = values == null ? new ContentValues() : new ContentValues(values);
        if (!v.containsKey(NotePad.Notes.COLUMN_NAME_MODIFIED)) {
            // 调用方主动给了修改时间（恢复备份、写演示数据）时尊重原值，否则打当前时间
            v.put(NotePad.Notes.COLUMN_NAME_MODIFIED, System.currentTimeMillis());
        }

        int rows;
        switch (match(uri)) {
            case NOTES:
                rows = helper.getWritableDatabase().update(TABLE, v, selection, selectionArgs);
                break;
            case NOTE_ID:
                rows = helper.getWritableDatabase().update(TABLE, v, withId(selection, uri),
                        withIdArgs(selectionArgs, uri));
                break;
            default:
                throw new IllegalArgumentException("未知 URI: " + uri);
        }
        if (rows > 0) {
            getContext().getContentResolver().notifyChange(NotePad.Notes.CONTENT_URI, null);
        }
        return rows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rows;
        switch (match(uri)) {
            case NOTES:
                rows = helper.getWritableDatabase().delete(TABLE, selection, selectionArgs);
                break;
            case NOTE_ID:
                rows = helper.getWritableDatabase().delete(TABLE, withId(selection, uri),
                        withIdArgs(selectionArgs, uri));
                break;
            default:
                throw new IllegalArgumentException("未知 URI: " + uri);
        }
        if (rows > 0) {
            getContext().getContentResolver().notifyChange(NotePad.Notes.CONTENT_URI, null);
        }
        return rows;
    }

    // ---------------------------------------------------------------- 工具方法

    private int match(Uri uri) {
        if (uri == null) {
            return 0;
        }
        if (NotePad.Notes.CONTENT_URI.equals(uri)) {
            return NOTES;
        }
        String last = uri.getLastPathSegment();
        if (last != null && last.matches("\\d+")) {
            return NOTE_ID;
        }
        return 0;
    }

    private String withId(String selection, Uri uri) {
        String idFilter = NotePad.Notes._ID + "=" + uri.getLastPathSegment();
        return selection == null || selection.isEmpty() ? idFilter : idFilter + " AND (" + selection + ")";
    }

    private String[] withIdArgs(String[] args, Uri uri) {
        return args == null ? null : args;
    }

    private static class DbHelper extends SQLiteOpenHelper {

        DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE + " ("
                    + NotePad.Notes._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_CREATED + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_MODIFIED + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_CATEGORY + " INTEGER DEFAULT "
                    + NotePad.CATEGORY_NONE + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 兼容 NotePad 原始库结构：老库只有 title/note/created/modified 四个业务字段
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN "
                        + NotePad.Notes.COLUMN_NAME_CATEGORY + " INTEGER DEFAULT "
                        + NotePad.CATEGORY_NONE);
            }
        }
    }
}
