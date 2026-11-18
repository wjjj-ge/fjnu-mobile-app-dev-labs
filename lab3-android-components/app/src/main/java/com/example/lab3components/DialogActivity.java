package com.example.lab3components;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 第 2 题：自定义布局的 AlertDialog。
 * 对话框完全不用系统的标题和按钮，而是把 dialog_signin.xml 整块交给 setView()，
 * 再自己接管 Cancel / Sign in 的点击。
 */
public class DialogActivity extends BaseDemoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        bindHeader(R.string.nav_02_title, "AlertDialog.Builder.setView()");

        findViewById(R.id.btnOpenDialog).setOnClickListener(v -> showSignInDialog());
    }

    private void showSignInDialog() {
        View content = getLayoutInflater().inflate(R.layout.dialog_signin, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
                .create();

        EditText etUser = content.findViewById(R.id.etUser);
        EditText etPwd = content.findViewById(R.id.etPwd);

        content.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        content.findViewById(R.id.btnSign).setOnClickListener(v -> {
            String user = etUser.getText().toString().trim();
            String pwd = etPwd.getText().toString().trim();
            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd)) {
                Toast.makeText(this, R.string.dialog_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, getString(R.string.dialog_sign_in, user), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();

        // 自定义布局自带白底圆角，要把对话框窗口的默认背景清掉，否则会多出一圈灰边。
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (300 * getResources().getDisplayMetrics().density);
            dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
