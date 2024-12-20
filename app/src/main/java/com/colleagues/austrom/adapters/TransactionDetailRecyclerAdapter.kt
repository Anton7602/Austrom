package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.TransactionType

class TransactionDetailRecyclerAdapter(private val transaction: Transaction, private val transactionDetails: List<TransactionDetail>): RecyclerView.Adapter<TransactionDetailRecyclerAdapter.TransactionDetailViewHolder>() {
    class TransactionDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionDetailHolder: LinearLayout = itemView.findViewById(R.id.trdetit_transactionDetailHolder_lly)
        val categoryImage: ImageView = itemView.findViewById(R.id.trdetit_category_img)
        val itemName: TextView = itemView.findViewById(R.id.trdetit_itemName_txt)
        val cost: TextView = itemView.findViewById(R.id.trdetit_cost_txt)
        val currency: TextView = itemView.findViewById(R.id.trdetit_currency_txt)
        val quantity: TextView = itemView.findViewById(R.id.trdetit_quantity_txt)
        val quantityType: TextView = itemView.findViewById(R.id.trdetit_quantityType_txt)
        val quantityHolder: LinearLayout = itemView.findViewById(R.id.trdetit_quantityHolder_lly)
    }
    private var returnClickedItem: (TransactionDetail)->Unit = {}
    fun setOnItemClickListener(l: ((TransactionDetail)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionDetailViewHolder { return TransactionDetailViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_detail, parent, false)) }
    override fun getItemCount(): Int { return transactionDetails.size  }

    override fun onBindViewHolder(holder: TransactionDetailViewHolder, position: Int) {
        val transactionDetail = transactionDetails[position]
        val categoryName = transactionDetail.categoryName ?: transaction.categoryId
        holder.itemName.text = transactionDetail.name
        holder.cost.text = transactionDetail.cost?.toMoneyFormat()
        holder.quantityHolder.visibility = if (transactionDetail.quantity==null) View.GONE else View.VISIBLE
        holder.quantity.text = transactionDetail.quantity.toString()
        holder.quantityType.text = transactionDetail.typeOfQuantity.toString()
        holder.currency.text = AustromApplication.activeCurrencies[transaction.assetId]?.symbol
        holder.categoryImage.setImageResource(when (transaction.transactionType()) {
            TransactionType.INCOME -> (AustromApplication.activeIncomeCategories.values.find { entry -> entry.name==categoryName })?.imgReference?.resourceId ?: R.drawable.ic_placeholder_icon
            TransactionType.TRANSFER -> (AustromApplication.activeTransferCategories.values.find { entry -> entry.name==categoryName })?.imgReference?.resourceId ?: R.drawable.ic_placeholder_icon
            TransactionType.EXPENSE -> (AustromApplication.activeExpenseCategories.values.find { entry -> entry.name==categoryName })?.imgReference?.resourceId ?: R.drawable.ic_placeholder_icon
        })
        holder.transactionDetailHolder.setOnClickListener { returnClickedItem(transactionDetail) }
    }
}