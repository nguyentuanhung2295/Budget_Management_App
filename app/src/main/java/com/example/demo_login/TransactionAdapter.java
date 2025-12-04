package com.example.demo_login;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Import
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private Context context;
    private List<Transaction> transactionList;

    private OnTransactionItemListener listener;

    public interface OnTransactionItemListener {
        void onEditClick(Transaction transaction);
        void onDeleteClick(int transactionId, int position);
    }

    public TransactionAdapter(Context context, List<Transaction> transactionList, OnTransactionItemListener listener) {
        this.context = context;
        this.transactionList = transactionList;
        this.listener = listener;
    }

    public void updateData(List<Transaction> newList) {
        this.transactionList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.tvCategory.setText(transaction.getCategoryName());
        holder.tvNote.setText(transaction.getDescription());
        holder.tvDate.setText(transaction.getDate());

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        String formattedAmount = formatter.format(transaction.getAmount());

        if ("income".equalsIgnoreCase(transaction.getType())) {
            holder.tvAmount.setText("+ " + formattedAmount);
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvAmount.setText("- " + formattedAmount);
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(transaction);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(transaction.getId(), position);
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvNote, tvAmount, tvDate;
        ImageView imgIcon;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_trans_category);
            tvNote = itemView.findViewById(R.id.tv_trans_note);
            tvAmount = itemView.findViewById(R.id.tv_trans_amount);
            tvDate = itemView.findViewById(R.id.tv_trans_date);
            imgIcon = itemView.findViewById(R.id.img_category_icon);

            btnEdit = itemView.findViewById(R.id.btn_edit_item);
            btnDelete = itemView.findViewById(R.id.btn_delete_item);
        }
    }
}