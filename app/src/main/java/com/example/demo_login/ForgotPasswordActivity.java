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

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Initialize Views from Layout
        etForgotEmail = findViewById(R.id.et_forgot_email);
        etRestoreCode = findViewById(R.id.etRestoreCode);
        etNewPassword = findViewById(R.id.et_new_password);
        etVerifyNewPassword = findViewById(R.id.et_verify_new_password);
        btnVerifyReset = findViewById(R.id.btn_verify_reset);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Establish button reset password
        btnVerifyReset.setOnClickListener(v -> handlePasswordReset());

        // Back to Login
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    private void handlePasswordReset() {
        String email = etForgotEmail.getText().toString().trim();
        String enteredCode = etRestoreCode.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String verifyNewPass = etVerifyNewPassword.getText().toString().trim();
        // 1. Check null
        if (email.isEmpty() || enteredCode.isEmpty() || newPass.isEmpty() || verifyNewPass.isEmpty()) {
            Toast.makeText(this, "Please enter all information!", Toast.LENGTH_SHORT).show();
            return;
        }
        // 2. Check exists email
        if (!dbHelper.checkUserExists(email)) {
            Toast.makeText(this, "Email do not exist in system.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 3. Take code from SQLite
        String storedCode = dbHelper.getVerifyCode(email);
        // 4. Check code verify to reset password
        if (storedCode == null || storedCode.isEmpty()) {
            Toast.makeText(this, "Code restore is not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!enteredCode.equals(storedCode)) {
            Toast.makeText(this, "Code restore is incorrect.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 5. Check new password and verify new password
        if (!newPass.equals(verifyNewPass)) {
            Toast.makeText(this, "New password and verify password are not the same!", Toast.LENGTH_SHORT).show();
            return;
        }
        // 6. Update password in SQLite
        boolean updateSuccess = dbHelper.updatePassword(email, newPass);
        if (updateSuccess) {
            Toast.makeText(this, "Password has been reset successfully!", Toast.LENGTH_LONG).show();
            // Back to Login
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error updating password.", Toast.LENGTH_SHORT).show();
        }
    }
}