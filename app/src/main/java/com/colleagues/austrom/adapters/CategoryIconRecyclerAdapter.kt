package com.colleagues.austrom.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.managers.Icon

class CategoryIconRecyclerAdapter(private var drawableIds: List<Icon>,
                                  private var parent: IDialogInitiator? = null,
                                  var selectedIcon: Icon = Icon.I0): RecyclerView.Adapter<CategoryIconRecyclerAdapter.CategoryIconViewHolder>() {
    private var selectedViewHolder: CategoryIconViewHolder? = null
    private var allViewHolders: MutableList<CategoryIconViewHolder> = mutableListOf()

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
        if (holder.icon == selectedIcon) {
            holder.iconHolder.backgroundTintList = ColorStateList.valueOf(Color.argb(255, 13,153,255))
            selectedViewHolder = holder
        }
        else {
            holder.iconHolder.backgroundTintList = ColorStateList.valueOf(Color.argb(255,230,230,230))
        }
        holder.iconHolder.setOnClickListener {
            changeSelectedIcon(holder)
        }
        allViewHolders.add(holder)
    }

    fun selectIcon(icon: Icon) {
        selectedIcon = icon
        allViewHolders.forEach{ holder ->
            if (holder.icon.resourceId == icon.resourceId) {
                changeSelectedIcon(holder)
                return
            }
        }
    }

    private fun changeSelectedIcon(holder: CategoryIconViewHolder) {
        if (selectedViewHolder!=null) {
            selectedViewHolder!!.iconHolder.backgroundTintList = ColorStateList.valueOf(Color.argb(255,230,230,230))
            selectedViewHolder!!.isSelected=false
        }
        holder.isSelected = true
        holder.iconHolder.backgroundTintList = ColorStateList.valueOf(Color.argb(255, 13,153,255))
        selectedViewHolder = holder
        selectedIcon = holder.icon
        if (parent!=null) {
            parent!!.receiveValue(holder.icon.resourceId.toString(), "SelectedIcon")
        }
    }
}