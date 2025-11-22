package com.example.demo_login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etForgotEmail, etRestoreCode, etNewPassword, etVerifyNewPassword;
    private Button btnVerifyReset;
    private DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Ánh xạ Views từ Layout
        etForgotEmail = findViewById(R.id.et_forgot_email);
        etRestoreCode = findViewById(R.id.etRestoreCode);
        etNewPassword = findViewById(R.id.et_new_password);
        etVerifyNewPassword = findViewById(R.id.et_verify_new_password);
        btnVerifyReset = findViewById(R.id.btn_verify_reset);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Thiết lập sự kiện cho nút "XÁC NHẬN & ĐẶT LẠI MẬT KHẨU"
        btnVerifyReset.setOnClickListener(v -> handlePasswordReset());

        // Thiết lập sự kiện quay lại màn hình Đăng nhập
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // ⭐ Hàm xử lý Logic Đặt lại Mật khẩu (đã được đưa vào trong lớp)
    private void handlePasswordReset() {
        String email = etForgotEmail.getText().toString().trim();
        String enteredCode = etRestoreCode.getText().toString().trim(); // Mã người dùng nhập
        String newPass = etNewPassword.getText().toString().trim();
        String verifyNewPass = etVerifyNewPassword.getText().toString().trim();

        // 1. Kiểm tra rỗng
        if (email.isEmpty() || enteredCode.isEmpty() || newPass.isEmpty() || verifyNewPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra Email có tồn tại
        if (!dbHelper.checkUserExists(email)) {
            Toast.makeText(this, "Email không tồn tại trong hệ thống.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Lấy mã xác thực ĐÃ LƯU TRONG CSDL
        String storedCode = dbHelper.getVerifyCode(email);

        // 4. KIỂM TRA MÃ XÁC THỰC
        if (storedCode == null || storedCode.isEmpty()) {
            Toast.makeText(this, "Mã khôi phục chưa được tạo hoặc đã hết hạn.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!enteredCode.equals(storedCode)) {
            // ⭐ SAI MÃ XÁC THỰC
            Toast.makeText(this, "Mã khôi phục KHÔNG CHÍNH XÁC.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!newPass.equals(verifyNewPass)) {
            Toast.makeText(this, "Mật khẩu mới và xác nhận mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 6. Cập nhật mật khẩu trong SQLite
        boolean updateSuccess = dbHelper.updatePassword(email, newPass);

        if (updateSuccess) {
            // 7. Xóa mã xác thực sau khi đặt lại mật khẩu thành công
            dbHelper.clearVerifyCode(email);

            Toast.makeText(this, "Mật khẩu đã được đặt lại thành công!", Toast.LENGTH_LONG).show();

            // Chuyển về màn hình Đăng nhập
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi đặt lại mật khẩu.", Toast.LENGTH_SHORT).show();
        }
    }
}