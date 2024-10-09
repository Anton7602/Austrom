package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CurrencyRecyclerAdapter.CurrencyViewHolder
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Currency

class CategoryIconRecyclerAdapter(private var drawableIds: List<Int>): RecyclerView.Adapter<CategoryIconRecyclerAdapter.CategoryIconViewHolder>() {
    class CategoryIconViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
        var iconHolder: ImageView = itemview.findViewById(R.id.caticit_icon_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryIconViewHolder {
        return CategoryIconViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_category_icon, parent, false))
    }

    override fun getItemCount(): Int {
        return drawableIds.size
    }

    override fun onBindViewHolder(holder: CategoryIconViewHolder, position: Int) {
        holder.iconHolder.setImageResource(drawableIds[position])
    }

}