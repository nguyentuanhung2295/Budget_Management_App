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

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private Context context;
    private List<Budget> list;
    private DatabaseHelper dbHelper;

    public BudgetAdapter(Context context, List<Budget> list, DatabaseHelper dbHelper) {
        this.context = context;
        this.list = list;
        this.dbHelper = dbHelper;
    }

    public void updateData(List<Budget> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = list.get(position);

        holder.tvCategory.setText(budget.getCategory());
        holder.tvDate.setText("Time: " + budget.getMonth() + "/" + budget.getYear());

        NumberFormat fmt = NumberFormat.getInstance(Locale.US);
        holder.tvAmount.setText(fmt.format(budget.getMaxAmount()) + " VND");

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Budget?")
                    .setMessage("Are you sure to delete for " + budget.getCategory() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (dbHelper.deleteBudget(budget.getId())) {
                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, list.size());
                            Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this.context, "Error deleting!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDate, tvAmount;
        ImageButton btnDelete; //

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvAmount = itemView.findViewById(R.id.tvItemAmount);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}