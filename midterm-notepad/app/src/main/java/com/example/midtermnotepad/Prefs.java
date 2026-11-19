package com.example.midtermnotepad;

import android.content.Context;
import android.content.SharedPreferences;

/** 偏好设置：主题、编辑器字号、默认排序、是否显示摘要。 */
public final class Prefs {

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SEPIA = 2;

    private static final String FILE = "notepad_prefs";
    private static final String KEY_THEME = "theme";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_SORT = "default_sort";
    private static final String KEY_SNIPPET = "show_snippet";

    private Prefs() {
    }

    private static SharedPreferences sp(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static int theme(Context context) {
        return sp(context).getInt(KEY_THEME, THEME_LIGHT);
    }

    public static void setTheme(Context context, int theme) {
        sp(context).edit().putInt(KEY_THEME, theme).apply();
    }

    public static int themeRes(int theme) {
        switch (theme) {
            case THEME_DARK:
                return R.style.Theme_NotePad_Dark;
            case THEME_SEPIA:
                return R.style.Theme_NotePad_Sepia;
            default:
                return R.style.Theme_NotePad_Light;
        }
    }

    public static int fontSize(Context context) {
        return sp(context).getInt(KEY_FONT_SIZE, 17);
    }

    public static void setFontSize(Context context, int sp) {
        sp(context).edit().putInt(KEY_FONT_SIZE, sp).apply();
    }

    public static int defaultSort(Context context) {
        return sp(context).getInt(KEY_SORT, NoteStore.SORT_MODIFIED);
    }

    public static void setDefaultSort(Context context, int sort) {
        sp(context).edit().putInt(KEY_SORT, sort).apply();
    }

    public static boolean showSnippet(Context context) {
        return sp(context).getBoolean(KEY_SNIPPET, true);
    }

    public static void setShowSnippet(Context context, boolean show) {
        sp(context).edit().putBoolean(KEY_SNIPPET, show).apply();
    }
}
