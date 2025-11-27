package com.example.demo_login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button btnLogin;
    private DatabaseHelper dbHelper;
    private TextView tvRegisterNow, tvForgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đây sẽ là Activity chính hiển thị khi khởi động
        setContentView(R.layout.activity_login);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Ánh xạ Views từ Layout
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterNow = findViewById(R.id.tv_register_now);
        tvForgotPass = findViewById(R.id.tvForgotPass);

        // Thiết lập sự kiện cho nút ĐĂNG NHẬP
        btnLogin.setOnClickListener(v -> handleLogin());

        // Thiết lập sự kiện chuyển sang màn hình Đăng ký
        tvRegisterNow.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        // Event Update password
        tvForgotPass.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = loginEmail.getText().toString().trim();
        String pass = loginPassword.getText().toString().trim();

        // 1. Kiểm tra đầu vào
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra thông tin đăng nhập với CSDL
        int userId = dbHelper.checkCredentials(email, pass);

        if (userId != -1) { // Đăng nhập thành công
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();

            // ⭐ BƯỚC 2: Tạo Intent và đính kèm userId
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("EXTRA_USER_ID", userId); // Đính kèm userId
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email hoặc Mật khẩu không đúng.", Toast.LENGTH_SHORT).show();
        }
    }
}