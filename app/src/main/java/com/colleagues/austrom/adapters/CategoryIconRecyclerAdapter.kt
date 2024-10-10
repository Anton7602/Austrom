package com.colleagues.austrom.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.managers.Icon

class CategoryIconRecyclerAdapter(private var drawableIds: List<Icon>): RecyclerView.Adapter<CategoryIconRecyclerAdapter.CategoryIconViewHolder>() {
    private var selectedViewHolder: CategoryIconViewHolder? = null
    var selectedIcon: Icon? = selectedViewHolder?.icon

    class CategoryIconViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var iconHolder: ImageView = itemView.findViewById(R.id.caticit_icon_img)
        var icon: Icon = Icon.I0
        var isSelected: Boolean = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryIconViewHolder {
        return CategoryIconViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_category_icon, parent, false))
    }

    override fun getItemCount(): Int {
        return drawableIds.size
    }

    override fun onBindViewHolder(holder: CategoryIconViewHolder, position: Int) {
        holder.icon = drawableIds[position]
        holder.iconHolder.setImageResource(holder.icon.resourceId)
        if (holder.isSelected) holder.iconHolder.setBackgroundResource(R.drawable.sh_card_background)
        else holder.iconHolder.setBackgroundColor(Color.TRANSPARENT)
        holder.iconHolder.setOnClickListener {
            if (selectedViewHolder!=null) {
                selectedViewHolder!!.iconHolder.setBackgroundColor(Color.TRANSPARENT)
                selectedViewHolder!!.isSelected=false
            }
            holder.isSelected = true
            holder.iconHolder.setBackgroundResource(R.drawable.sh_card_background)
            selectedViewHolder = holder
            selectedIcon = holder.icon
        }
    }
}