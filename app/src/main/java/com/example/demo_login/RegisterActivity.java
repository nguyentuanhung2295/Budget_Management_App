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

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Map Views from Layout
        regName = findViewById(R.id.reg_name);
        regEmail = findViewById(R.id.reg_email);
        regPassword = findViewById(R.id.reg_password);
        regVerifyPassword = findViewById(R.id.reg_verify_password);
        edtPassCode = findViewById(R.id.edtCodePass);
        btnRegister = findViewById(R.id.btn_register);
        TextView tv_back_to_login = findViewById(R.id.tv_back_to_login);

        // Set up click listener for REGISTER button
        btnRegister.setOnClickListener(v -> handleRegistration());

        // Set up click listener to return to Login screen
        tv_back_to_login.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close current Activity
        });
    }

    private void handleRegistration() {
        String name = regName.getText().toString().trim();
        String email = regEmail.getText().toString().trim();
        String pass = regPassword.getText().toString().trim();
        String verifyPass = regVerifyPassword.getText().toString().trim();
        String codePass = edtPassCode.getText().toString().trim();

        // 1. Check for empty inputs
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || verifyPass.isEmpty() || codePass.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Check if passwords match
        if (!pass.equals(verifyPass)) {
            Toast.makeText(this, "Password and confirm password do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Check if Email already exists in DB
        if (dbHelper.checkUserExists(email)) {
            Toast.makeText(this, "Email already exists. Please login!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Check if recovery code is a valid integer
        try {
            // Try to parse string to integer
            int code = Integer.parseInt(codePass);
        } catch (NumberFormatException e) {
            // If error (not a number), show message
            Toast.makeText(this, "Recovery code must be an integer!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Add user to DB
        long result = dbHelper.addUser(name, email, pass, codePass);

        if (result > 0) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show();
            // Navigate to Login screen after successful registration
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}