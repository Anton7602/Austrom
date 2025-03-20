package com.colleagues.austrom.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.views.MoneyFormatTextView

class TransactionDetailEditRecyclerAdapter(private val transactionDetailsList: MutableList<TransactionDetail>, private val context: Context) : RecyclerView.Adapter<TransactionDetailEditRecyclerAdapter.TransactionDetailEditViewHolder>() {
    inner class TransactionDetailEditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.trdetedit_detailName_txt)
        val amountTextView: MoneyFormatTextView = itemView.findViewById(R.id.trdetedit_amount_monf)
        val deleteButton: ImageButton = itemView.findViewById(R.id.trdetedit_removeButton_btn)
    }
    private var returnClickedItem: (TransactionDetail)->Unit = {}
    fun setOnItemClickListener(l: ((TransactionDetail)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionDetailEditViewHolder { return TransactionDetailEditViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_detail_edit, parent, false)) }
    override fun getItemCount(): Int { return transactionDetailsList.size }

    override fun onBindViewHolder(holder: TransactionDetailEditViewHolder, position: Int) {
        val transactionDetail = transactionDetailsList[position]
        holder.nameTextView.text = transactionDetail.name
        holder.amountTextView.setValue(transactionDetail.cost)
        holder.deleteButton.setOnClickListener { returnClickedItem(transactionDetail) }
    }

    fun removeItem(transactionDetail: TransactionDetail) {
        val index = transactionDetailsList.indexOf(transactionDetail)
        if (index>0) {
            transactionDetailsList.remove(transactionDetail)
            this.notifyItemRemoved(index)
        }
    }
}