package com.example.demo_login;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification); // Tạo layout này chỉ chứa 1 RecyclerView

        int userId = getIntent().getIntExtra("EXTRA_USER_ID", -1);
        if (userId == -1) { finish(); return; }

        RecyclerView rv = findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper db = new DatabaseHelper(this);
        List<NotificationItem> list = db.getUserNotifications(userId);

        NotificationAdapter adapter = new NotificationAdapter(list);
        rv.setAdapter(adapter);

        // Setup Footer
        FooterActivity.setupFooterListeners(this, userId);
    }
}