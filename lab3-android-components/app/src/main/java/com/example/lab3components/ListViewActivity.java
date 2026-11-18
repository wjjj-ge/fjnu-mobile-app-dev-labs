package com.example.lab3components;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 第 1 题：SimpleAdapter 驱动的 ListView。
 * 点击行为拆成两件事：先 Toast（轻提示），再发一条通知（常驻通知栏）。
 * 通知的 Title 取被点中的动物名，正文自拟；图标用应用自己的 icon。
 */
public class ListViewActivity extends BaseDemoActivity {

    private static final String CHANNEL_ID = "lab3_list";
    private static final int REQ_NOTIFY = 1001;

    private final int[] photos = {
            R.drawable.lion, R.drawable.tiger, R.drawable.monkey,
            R.drawable.dog, R.drawable.cat, R.drawable.elephant
    };

    private int notifyId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        bindHeader(R.string.nav_01_title, "SimpleAdapter / Toast / Notification");

        ensureChannel();
        askNotificationPermission();

        String[] names = getResources().getStringArray(R.array.animal_names);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("name", names[i]);
            row.put("icon", photos[i % photos.length]);
            rows.add(row);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                rows,
                R.layout.item_animal,
                new String[]{"name", "icon"},
                new int[]{R.id.tvName, R.id.ivIcon});
        ListView list = findViewById(R.id.listAnimals);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            String name = (String) rows.get(position).get("name");
            Toast.makeText(this, getString(R.string.toast_selected, name), Toast.LENGTH_SHORT).show();
            postNotification(name);
        });
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notify_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) return;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFY);
    }

    private void postNotification(String name) {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.notify_denied, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, ListViewActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(appIcon())
                .setContentTitle(name)
                .setContentText(getString(R.string.notify_text, name))
                .setContentIntent(pending)
                .setAutoCancel(true);

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notifyId++, builder.build());
        }
    }

    /** 取应用自己的图标，转成 Bitmap 给通知的 largeIcon 用（小图标会被系统压成单色剪影）。 */
    private Bitmap appIcon() {
        try {
            Drawable drawable = getPackageManager().getApplicationIcon(getPackageName());
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
