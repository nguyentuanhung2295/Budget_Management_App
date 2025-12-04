package com.example.demo_login;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    // Views
    private TextView tvName, tvEmail, tvPassword;
    private Button btnBudget, btnRecurring, btnLogout;

    // Logic
    private DatabaseHelper dbHelper;
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        dbHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("EXTRA_USER_ID")) {
            currentUserId = getIntent().getIntExtra("EXTRA_USER_ID", -1);
        }
        if (currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvName = findViewById(R.id.tvSettingName);
        tvEmail = findViewById(R.id.tvSettingEmail);
        tvPassword = findViewById(R.id.tvSettingPassword);
        btnBudget = findViewById(R.id.btnGoToBudget);
        btnRecurring = findViewById(R.id.btnGoToRecurring);
        btnLogout = findViewById(R.id.btnLogout);
        loadUserProfile();

        btnBudget.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, BudgetActivity.class);
            intent.putExtra("EXTRA_USER_ID", currentUserId);
            startActivity(intent);
        });

        btnRecurring.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, RecurringActivity.class);
            intent.putExtra("EXTRA_USER_ID", currentUserId);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        FooterActivity.setupFooterListeners(this, currentUserId);
    }

    private void loadUserProfile() {
        Cursor cursor = dbHelper.getUserById(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));

            tvName.setText(name);
            tvEmail.setText(email);
            tvPassword.setText("********");

            cursor.close();
        } else {
            Toast.makeText(this, "Can not load user profile", Toast.LENGTH_SHORT).show();
        }
    }
}