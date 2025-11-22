package com.example.demo_login;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final Context context;
    private final Cursor cursor;

    public TransactionAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        cursor.moveToPosition(position);

        String cate   = cursor.getString(cursor.getColumnIndexOrThrow("category"));
        String date   = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        String note   = cursor.getString(cursor.getColumnIndexOrThrow("note"));
        double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));

        holder.tvCategory.setText(cate);
        holder.tvDate.setText(date);
        holder.tvNote.setText(note);
        holder.tvAmount.setText(String.valueOf(amount));
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvDate, tvNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvAmount   = itemView.findViewById(R.id.tvItemAmount);
            tvDate     = itemView.findViewById(R.id.tvItemDate);
            tvNote     = itemView.findViewById(R.id.tvItemNote);
        }
    }
}
