package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import java.time.format.DateTimeFormatter

class TransactionRecyclerAdapter(private val transactions: List<Transaction>) : RecyclerView.Adapter<TransactionRecyclerAdapter.TransactionViewHolder>() {
    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.tritem_categoryName_txt)
        val categoryImage: ImageView = itemView.findViewById(R.id.tritem_categoryIcon_img)
        val transactionDate: TextView = itemView.findViewById(R.id.tritem_date_txt)
        val amount: TextView = itemView.findViewById(R.id.tritem_amount_txt)
        val sourceName: TextView = itemView.findViewById(R.id.tritem_sourceName_txt)
        val targetName: TextView = itemView.findViewById(R.id.tritem_targetName_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.categoryName.text = transactions[position].categoryId
        holder.amount.text = transactions[position].amount.toString()
        holder.sourceName.text = transactions[position].sourceName
        holder.targetName.text = transactions[position].targetName
        holder.transactionDate.text =
            "${transactions[position].transactionDate?.dayOfWeek} ${transactions[position].transactionDate?.format(DateTimeFormatter.ofPattern("dd.MM"))}"
    }
}

