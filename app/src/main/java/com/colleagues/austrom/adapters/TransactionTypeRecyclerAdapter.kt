package com.colleagues.austrom.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.TransactionType

class TransactionTypeRecyclerAdapter(private val transactionTypes: List<TransactionType>, private val context: Context): RecyclerView.Adapter<TransactionTypeRecyclerAdapter.TransactionTypeViewHolder>() {
    class TransactionTypeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val transactionTypeHolder: LinearLayout = itemView.findViewById(R.id.ittrantype_assetTypeHolder_lly)
        val transactionTypeName: TextView = itemView.findViewById(R.id.ittranstype_assetTypeName_txt)
        val transactionTypeDesc: TextView = itemView.findViewById(R.id.ittranstype_assetTypeDescr_txt)
        val transactionTypeIcon: ImageView = itemView.findViewById(R.id.ittrantype_assetTypeIcon_img)
    }
    private var returnClickedItem: (TransactionType)->Unit = {}
    fun setOnItemClickListener(l: ((TransactionType)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionTypeViewHolder { return TransactionTypeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_type, parent, false))  }
    override fun getItemCount(): Int { return transactionTypes.size  }

    override fun onBindViewHolder(holder: TransactionTypeViewHolder, position: Int) {
        val transactionType = transactionTypes[position]
        holder.transactionTypeName.text = context.getString(transactionType.transactionTypeNameId)
        holder.transactionTypeDesc.text = context.getString(transactionType.transactionTypeDescriptionResourceId)
        holder.transactionTypeIcon.setImageResource(transactionType.transactionTypeIconResource)
        holder.transactionTypeHolder.setOnClickListener { returnClickedItem(transactionType) }
    }
}