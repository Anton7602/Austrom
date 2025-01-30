package com.colleagues.austrom.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Category

class SelectorRecyclerAdapter(private val items: List<String>): RecyclerView.Adapter<SelectorRecyclerAdapter.ViewHolder>() {
    private var returnClickedItem: (String)->Unit = {}
    fun setOnItemClickListener(l: ((String)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_selector, parent, false))  }
    override fun getItemCount(): Int = items.size
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
    }

    private var selectedPosition = -1

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.radioButton.text = item
        holder.radioButton.isChecked = position == selectedPosition
        holder.radioButton.setOnClickListener {
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()
            returnClickedItem(item)
        }
    }
}