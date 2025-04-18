package com.colleagues.austrom.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.views.MoneyFormatTextView

class TransactionGroupRecyclerAdapter(private val groupedTransactions: MutableMap<String, MutableList<Transaction>>, private val context: Context) : RecyclerView.Adapter<TransactionGroupRecyclerAdapter.TransactionGroupViewHolder>(){
    class TransactionGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionGroupName: TextView = itemView.findViewById(R.id.trgritem_date_txt)
        val transactionHolderRecyclerView: RecyclerView= itemView.findViewById(R.id.trgritem_transactionholder_rcv)
        val transactionGroupSumHolder: MoneyFormatTextView= itemView.findViewById(R.id.trgritem_sumHolder_mfor)
        val transactionCollapseButton: Button= itemView.findViewById(R.id.trgritem_collapseButton_btn)
    }
    private var returnClickedItem: (transaction: Transaction, index: Int)->Unit = {_,_ ->}
    fun setOnItemClickListener(l: ((Transaction, Int)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionGroupViewHolder { return TransactionGroupViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_group, parent, false)) }
    override fun getItemCount(): Int { return groupedTransactions.size }
    init {
        groupedTransactions.forEach { group -> group.value.sortBy { transaction -> transaction.amount } }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionGroupViewHolder, position: Int) {
        holder.transactionGroupName.text = groupedTransactions.keys.elementAt(position)
        holder.transactionHolderRecyclerView.layoutManager = LinearLayoutManager(context)
        val transactionsSum = groupedTransactions.values.elementAt(position).sumOf { transaction -> transaction.amount }
        val moneyColor = if (transactionsSum>0) context.getColor(R.color.incomeGreen) else if (transactionsSum<0) context.getColor(R.color.expenseRed) else context.getColor(R.color.transferYellow)
        holder.transactionGroupSumHolder.setAmountColor(moneyColor)
        holder.transactionGroupSumHolder.setCurrencyColor(moneyColor)
        holder.transactionGroupSumHolder.setValue(transactionsSum)
        val adapter = TransactionRecyclerAdapter(groupedTransactions.values.elementAt(position), context)
        adapter.setOnItemClickListener { transaction, _ -> returnClickedItem(transaction, position) }
        holder.transactionHolderRecyclerView.adapter = adapter
        holder.transactionCollapseButton.setOnSafeClickListener { holder.transactionHolderRecyclerView.visibility = if (holder.transactionHolderRecyclerView.visibility==View.VISIBLE) View.GONE else View.VISIBLE }
    }
}