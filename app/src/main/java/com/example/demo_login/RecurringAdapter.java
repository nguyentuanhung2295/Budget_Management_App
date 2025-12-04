package com.example.demo_login;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecurringAdapter extends RecyclerView.Adapter<RecurringAdapter.ViewHolder> {

    private Context context;
    private List<RecurringExpense> list;
    private DatabaseHelper dbHelper;

    public RecurringAdapter(Context context, List<RecurringExpense> list, DatabaseHelper dbHelper) {
        this.context = context;
        this.list = list;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recurring, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecurringExpense item = list.get(position);

        holder.tvCategory.setText(item.getCategory());
        holder.tvFrequency.setText(item.getFrequency());
        holder.tvDate.setText("Start: " + item.getStartDate());

        NumberFormat fmt = NumberFormat.getInstance(Locale.US);
        holder.tvAmount.setText(fmt.format(item.getAmount()) + " VND");

        // Sự kiện xóa
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Recurring?")
                    .setMessage("Are you sure you want to stop this recurring expense?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (dbHelper.deleteRecurring(item.getId())) {
                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, list.size());
                            Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvFrequency, tvDate, tvAmount;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvRecurCategory);
            tvFrequency = itemView.findViewById(R.id.tvRecurFrequency);
            tvDate = itemView.findViewById(R.id.tvRecurDate);
            tvAmount = itemView.findViewById(R.id.tvRecurAmount);
            btnDelete = itemView.findViewById(R.id.btnDeleteRecur);
        }
    }
}
