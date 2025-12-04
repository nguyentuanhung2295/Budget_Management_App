package com.example.demo_login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BudgetActivity extends AppCompatActivity {

    private Spinner spCategory, spMonth, spYear;
    private EditText etBudgetAmount;
    private Button btnSave;
    private RecyclerView recyclerView;

    private DatabaseHelper db;
    private BudgetAdapter adapter;
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        db = new DatabaseHelper(this);

        if (getIntent().hasExtra("EXTRA_USER_ID")) {
            currentUserId = getIntent().getIntExtra("EXTRA_USER_ID", -1);
        }
        if (currentUserId == -1) {
            finish();
            return;
        }

        spCategory = findViewById(R.id.spBudgetCategory);
        spMonth = findViewById(R.id.spMonth);
        spYear = findViewById(R.id.spYear);
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        btnSave = findViewById(R.id.btnSaveBudget);
        recyclerView = findViewById(R.id.recycler_budget);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load Data Spinner
        loadCategories();
        loadMonth();
        loadYear();

        loadBudgetList();

        btnSave.setOnClickListener(v -> saveBudget());

        // Footer
        FooterActivity.setupFooterListeners(this, currentUserId);
    }

    private void loadCategories() {
        String[] categories = {"Health", "Education", "Vehicle", "Phone", "Rent", "Saving", "Petrol", "Parking", "Invest", "Charity", "F&B", "Lifestyle"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    private void loadMonth() {
        Integer[] m = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(adapter);
    }

    private void loadYear() {
        Integer[] y = {2024, 2025, 2026};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, y);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(adapter);
    }
    private void loadBudgetList() {
        List<Budget> list = db.getBudgets(currentUserId);

        if (adapter == null) {
            adapter = new BudgetAdapter(this, list, db);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(list);
        }
    }

    private void saveBudget() {
        String category = spCategory.getSelectedItem().toString();
        String amountStr = etBudgetAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            etBudgetAmount.setError("Please enter amount");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            int month = (int) spMonth.getSelectedItem();
            int year = (int) spYear.getSelectedItem();

            boolean ok = db.setBudget(currentUserId, category, amount, month, year);

            if (ok) {
                Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                etBudgetAmount.setText("");
                loadBudgetList();
            } else {
                Toast.makeText(this, "Save Failed!", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            etBudgetAmount.setError("Invalid number");
        }
    }
}