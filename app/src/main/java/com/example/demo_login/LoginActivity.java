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
        // This is main activity when start app
        setContentView(R.layout.activity_login);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Initialize Views from Layout
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterNow = findViewById(R.id.tv_register_now);
        tvForgotPass = findViewById(R.id.tvForgotPass);

        // Establish button Login event
        btnLogin.setOnClickListener(v -> handleLogin());

        // Establish event navigate to Register Activity
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

        // 1. Check input
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter email and password!", Toast.LENGTH_SHORT).show();
            return;
        }
        // 2. Check credentials with DatabaseHelper
        int userId = dbHelper.checkCredentials(email, pass);

        if (userId != -1) { // Login successfully
            Toast.makeText(this, "Login successfully!", Toast.LENGTH_LONG).show();

            // ⭐ Create Intent và attached userId
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("EXTRA_USER_ID", userId); // attached userId
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email or Password is incorrect.", Toast.LENGTH_SHORT).show();
        }
    }
}