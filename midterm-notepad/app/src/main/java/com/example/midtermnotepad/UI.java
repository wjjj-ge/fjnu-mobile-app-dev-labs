package com.example.midtermnotepad;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.Window;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.Locale;

/** 主题、分类配色、边缘到边缘（edge-to-edge）与搜索高亮这些小工具。 */
public final class UI {

    private UI() {
    }

    /** 在 super.onCreate() 之前调用，按偏好换主题。 */
    public static void applyTheme(Activity activity) {
        activity.setTheme(Prefs.themeRes(Prefs.theme(activity)));
    }

    /**
     * API 36 强制 edge-to-edge：状态栏透明、图标颜色随主题切换，
     * 并把系统栏占位以 padding 的形式让给页头（顶部）和底部列表（底部），
     * 这样背景不会被切出一条色带。
     */
    public static void edgeToEdge(Activity activity, View topView, View bottomView) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        boolean lightIcons = Prefs.theme(activity) != Prefs.THEME_DARK;
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(lightIcons);
        controller.setAppearanceLightNavigationBars(lightIcons);

        if (topView == null && bottomView == null) {
            return;
        }
        View root = activity.findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (topView != null) {
                topView.setPadding(topView.getPaddingLeft(), bars.top,
                        topView.getPaddingRight(), topView.getPaddingBottom());
            }
            if (bottomView != null) {
                bottomView.setPadding(bottomView.getPaddingLeft(), bottomView.getPaddingTop(),
                        bottomView.getPaddingRight(), bars.bottom);
            }
            return insets;
        });
    }

    public static int categoryColor(int category) {
        switch (category) {
            case NotePad.CATEGORY_LIFE:
                return Color.parseColor("#E8846B");
            case NotePad.CATEGORY_STUDY:
                return Color.parseColor("#4C8DFF");
            case NotePad.CATEGORY_WORK:
                return Color.parseColor("#2E9E6B");
            case NotePad.CATEGORY_IDEA:
                return Color.parseColor("#B26BE8");
            default:
                return Color.parseColor("#9AA6AF");
        }
    }

    public static String categoryLabel(int category) {
        switch (category) {
            case NotePad.CATEGORY_LIFE:
                return "生活";
            case NotePad.CATEGORY_STUDY:
                return "学习";
            case NotePad.CATEGORY_WORK:
                return "工作";
            case NotePad.CATEGORY_IDEA:
                return "灵感";
            default:
                return "未分类";
        }
    }

    /** 列表摘要：压掉换行，截到 120 字；有关键词时围绕第一处命中截取。 */
    public static String snippet(String note, String keyword) {
        if (note == null) {
            return "";
        }
        String flat = note.replaceAll("[\\r\\n]+", " ").trim();
        if (keyword != null && !keyword.isEmpty()) {
            int at = flat.toLowerCase(Locale.CHINA).indexOf(keyword.toLowerCase(Locale.CHINA));
            if (at >= 0) {
                int start = Math.max(0, at - 24);
                int end = Math.min(flat.length(), at + keyword.length() + 60);
                String cut = flat.substring(start, end);
                return (start > 0 ? "…" : "") + cut + (end < flat.length() ? "…" : "");
            }
        }
        return flat.length() <= 120 ? flat : flat.substring(0, 120) + "…";
    }
}
