package com.example.midtermnotepad;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.Uri;

/**
 * 演示数据：只在 debug 构建、且库里一条笔记都没有时写入，
 * 用来让截图里的「相对时间」「分类」「摘要」都有内容可看。
 * release 构建不会插入任何数据。
 */
final class Seed {

    private Seed() {
    }

    static void seedIfEmpty(Context context) {
        if (!isDebuggable(context)) {
            return;
        }
        if (NoteStore.count(context) > 0) {
            return;
        }

        long now = System.currentTimeMillis();
        long minute = 60_000L;
        long hour = 3_600_000L;
        long day = 24 * hour;

        insert(context, "期中实验：NotePad 改造清单", NotePad.CATEGORY_WORK, now - 2 * minute, now - 40_000L,
                "已完成：\n"
                        + "1. 列表条目加时间戳（相对时间 + 完整时间两种读法）\n"
                        + "2. 按标题或正文查询，命中处高亮\n"
                        + "3. 分类、排序、主题、导出与备份\n\n"
                        + "待办：写 README，补实机截图说明。");

        insert(context, "周末采购清单", NotePad.CATEGORY_LIFE, now - 5 * hour, now - 3 * hour,
                "牛奶、鸡蛋、西红柿\n挂面两包\n洗衣液（补充装）\n\n"
                        + "顺路取快递：京东 3 件、菜鸟 1 件。");

        insert(context, "《移动软件开发》复习要点", NotePad.CATEGORY_STUDY, now - day, now - 6 * hour,
                "Activity 生命周期九个回调的上屏顺序\n"
                        + "LinearLayout 权重与 TableLayout 列拉伸\n"
                        + "ContentProvider 的 CRUD 与 notifyChange\n"
                        + "RecyclerView 局部刷新 notifyItemInserted / notifyItemRangeChanged");

        insert(context, "地铁上的晚霞", NotePad.CATEGORY_IDEA, now - 2 * day, now - day - 2 * hour,
                "七点二十，从师大站上车，靠窗那侧整片天是橘粉色的。\n"
                        + "想起一句：日子过得快，但晚霞每天都按时到。");

        insert(context, "英语单词：Unit 7", NotePad.CATEGORY_STUDY, now - 4 * day, now - 3 * day,
                "allocate / assignment / burden / compensate\n"
                        + "priority / reassure / threshold / venue\n\n"
                        + "记忆法：把 allocate 和「分配预算」绑在一起记。");

        insert(context, "随手记", NotePad.CATEGORY_NONE, now - 9 * day, now - 8 * day,
                "实验室 302 的打印机又卡纸了，下次提前十分钟去。");
    }

    private static void insert(Context context, String title, int category,
                               long created, long modified, String note) {
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, note);
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, category);
        values.put(NotePad.Notes.COLUMN_NAME_CREATED, created);
        values.put(NotePad.Notes.COLUMN_NAME_MODIFIED, modified);
        Uri uri = context.getContentResolver().insert(NotePad.Notes.CONTENT_URI, values);
        if (uri != null) {
            // Provider 的 insert 会把 modified 打成当前时间，这里再补回演示用的时间
            ContentValues fix = new ContentValues();
            fix.put(NotePad.Notes.COLUMN_NAME_CREATED, created);
            fix.put(NotePad.Notes.COLUMN_NAME_MODIFIED, modified);
            context.getContentResolver().update(uri, fix, null, null);
        }
    }

    private static boolean isDebuggable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
