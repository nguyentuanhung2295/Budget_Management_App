package com.example.demo_login;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MonthlyReportActivity extends AppCompatActivity {

    Spinner spMonth, spYear;
    TextView tvIncome, tvExpense, tvBalance;
    RecyclerView rvList;

    DatabaseHelper db;
    int userId = 1; // TODO: thay bằng user đăng nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);

        db = new DatabaseHelper(this);

        spMonth = findViewById(R.id.spReportMonth);
        spYear = findViewById(R.id.spReportYear);
        tvIncome = findViewById(R.id.tvReportIncome);
        tvExpense = findViewById(R.id.tvReportExpense);
        tvBalance = findViewById(R.id.tvReportBalance);
        rvList = findViewById(R.id.rvMonthlyTransactions);

        loadMonth();
        loadYear();

        findViewById(R.id.btnGenerateReport).setOnClickListener(v -> loadReport());
    }

    private void loadMonth() {
        Integer[] m = {1,2,3,4,5,6,7,8,9,10,11,12};
        spMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m));
    }

    private void loadYear() {
        Integer[] y = {2024, 2025, 2026};
        spYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, y));
    }

    private void loadReport() {
        int month = (int) spMonth.getSelectedItem();
        int year = (int) spYear.getSelectedItem();

        double income = db.getMonthlyIncome(userId, month, year);
        double expense = db.getMonthlyExpense(userId, month, year);
        double balance = income - expense;

        tvIncome.setText("Tổng thu: " + income);
        tvExpense.setText("Tổng chi: " + expense);
        tvBalance.setText("Còn lại: " + balance);

        Cursor cursor = db.getMonthlyTransactions(userId, month, year);

        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(new TransactionAdapter(this, cursor));
    }
}