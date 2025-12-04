package com.example.demo_login;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import java.util.List;

public class RecurringActivity extends AppCompatActivity {

    Spinner spCategory, spFrequency;
    EditText etAmount;
    TextView tvStartDate, tvEndDate;
    Button btnSave;
    RecyclerView recyclerView;

    DatabaseHelper db;
    int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring);

        db = new DatabaseHelper(this);
        if (getIntent().hasExtra("EXTRA_USER_ID")) {
            userId = getIntent().getIntExtra("EXTRA_USER_ID", -1);
        }
        if (userId == -1) { finish(); return; }

        // Ánh xạ
        spCategory = findViewById(R.id.spRecurCategory);
        spFrequency = findViewById(R.id.spFrequency);
        etAmount = findViewById(R.id.etRecurAmount);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        btnSave = findViewById(R.id.btnSaveRecur);
        recyclerView = findViewById(R.id.rvRecurring);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupSpinners();
        loadRecurringList();

        // Sự kiện DatePicker
        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate));

        // Sự kiện Lưu
        btnSave.setOnClickListener(v -> saveRecurring());

        // Footer
        FooterActivity.setupFooterListeners(this, userId);
    }

    private void setupSpinners() {
        String[] categories = {"Rent", "Netflix", "Internet", "Gym", "Insurance", "Loan"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        String[] freq = {"Daily", "Weekly", "Monthly", "Yearly"};
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, freq);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrequency.setAdapter(freqAdapter);
    }

    private void showDatePicker(TextView target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = year + "-" + (month + 1) + "-" + day;
            target.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveRecurring() {
        String amountStr = etAmount.getText().toString();
        if (amountStr.isEmpty()) {
            etAmount.setError("Required");
            return;
        }
        if (tvStartDate.getText().toString().equals("Select")) {
            Toast.makeText(this, "Select Start Date", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String category = spCategory.getSelectedItem().toString();
        String freq = spFrequency.getSelectedItem().toString();
        String start = tvStartDate.getText().toString();
        String end = tvEndDate.getText().toString(); // Có thể là "Optional"

        boolean success = db.addRecurring(userId, category, amount, freq, start, end, "Active");
        if (success) {
            Toast.makeText(this, "Scheduled Successfully!", Toast.LENGTH_SHORT).show();
            loadRecurringList();
            etAmount.setText("");
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRecurringList() {
        List<RecurringExpense> list = db.getRecurringList(userId);
        RecurringAdapter adapter = new RecurringAdapter(this, list, db);
        recyclerView.setAdapter(adapter);
    }
}