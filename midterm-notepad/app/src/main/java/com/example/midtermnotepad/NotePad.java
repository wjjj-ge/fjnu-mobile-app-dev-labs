package com.example.midtermnotepad;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 笔记的契约类。命名与 AOSP NotePad 示例保持一致（{@code Notes.CONTENT_URI}、
 * {@code COLUMN_NAME_TITLE} / {@code COLUMN_NAME_NOTE}），在原有字段之外增加了
 * 创建时间、修改时间、分类与置顶标记——期中实验的时间戳与分类功能都落在这张表上。
 */
public final class NotePad {

    public static final String AUTHORITY = "com.example.midtermnotepad.provider";

    /** 未分类 */
    public static final int CATEGORY_NONE = -1;
    public static final int CATEGORY_LIFE = 0;
    public static final int CATEGORY_STUDY = 1;
    public static final int CATEGORY_WORK = 2;
    public static final int CATEGORY_IDEA = 3;

    public static final int[] CATEGORIES =
            {CATEGORY_LIFE, CATEGORY_STUDY, CATEGORY_WORK, CATEGORY_IDEA};

    private NotePad() {
    }

    public static final class Notes implements BaseColumns {

        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/notes");

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.midterm.note";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.midterm.note";

        /** 标题 */
        public static final String COLUMN_NAME_TITLE = "title";
        /** 正文 */
        public static final String COLUMN_NAME_NOTE = "note";
        /** 创建时间（毫秒） */
        public static final String COLUMN_NAME_CREATED = "created";
        /** 最后修改时间（毫秒），列表时间戳读的就是它 */
        public static final String COLUMN_NAME_MODIFIED = "modified";
        /** 分类，取值见 {@link NotePad} 的 CATEGORY_* 常量 */
        public static final String COLUMN_NAME_CATEGORY = "category";

        public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_MODIFIED + " DESC";

        private Notes() {
        }
    }
}
