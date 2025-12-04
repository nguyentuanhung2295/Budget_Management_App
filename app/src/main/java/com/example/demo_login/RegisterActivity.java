package com.example.demo_login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private EditText regName, regEmail, regPassword, regVerifyPassword, edtPassCode;
    private Button btnRegister;
    private DatabaseHelper dbHelper;
    private TextView tv_back_to_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Ánh xạ Views từ Layout
        regName = findViewById(R.id.reg_name);
        regEmail = findViewById(R.id.reg_email);
        regPassword = findViewById(R.id.reg_password);
        regVerifyPassword = findViewById(R.id.reg_verify_password);
        edtPassCode = findViewById(R.id.edtCodePass);
        btnRegister = findViewById(R.id.btn_register);
        TextView tv_back_to_login = findViewById(R.id.tv_back_to_login);

        // Thiết lập sự kiện cho nút ĐĂNG KÝ
        btnRegister.setOnClickListener(v -> handleRegistration());

        // Thiết lập sự kiện quay lại màn hình Đăng nhập
        tv_back_to_login.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng Activity hiện tại
        });
    }

    private void handleRegistration() {
        String name = regName.getText().toString().trim();
        String email = regEmail.getText().toString().trim();
        String pass = regPassword.getText().toString().trim();
        String verifyPass = regVerifyPassword.getText().toString().trim();
        String codePass = edtPassCode.getText().toString().trim();


        // 1. Kiểm tra đầu vào
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || verifyPass.isEmpty() || codePass.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra xác nhận mật khẩu
        if (!pass.equals(verifyPass)) {
            Toast.makeText(this, "Mật khẩu và xác nhận mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Kiểm tra Email đã tồn tại trong CSDL chưa
        if (dbHelper.checkUserExists(email)) {
            Toast.makeText(this, "Email đã tồn tại. Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Check code pass to correctly just integer
        try {
            // Cố gắng chuyển đổi chuỗi sang số nguyên
            int code = Integer.parseInt(codePass);
        } catch (NumberFormatException e) {
            // Nếu lỗi (không phải số), hiển thị thông báo
            Toast.makeText(this, "Mã xác thực phải là số nguyên", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Thêm người dùng vào CSDL
        long result = dbHelper.addUser(name, email, pass, codePass);

        if (result > 0) {
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
            // Chuyển về màn hình Đăng nhập sau khi đăng ký thành công
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
}