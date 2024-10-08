package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Category

class CategoryRecyclerAdapter(private val categories: List<Category>) : RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryViewHolder>() {
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.itcat_categoryName_txt)
        val categoryImage: ImageView = itemView.findViewById(R.id.itcat_categoruIcon_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false))
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.categoryName.text = categories[position].name
        holder.categoryImage.setImageResource(categories[position].imgReference ?: R.drawable.ic_placeholder_icon)
    }
}