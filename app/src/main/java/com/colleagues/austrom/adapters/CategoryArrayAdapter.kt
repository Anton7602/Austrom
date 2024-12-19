package com.colleagues.austrom.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Category

class CategoryArrayAdapter (context: Context, categories: List<Category>) : ArrayAdapter<Category>(context, 0, categories) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
        }

        val category = getItem(position)
        if (category!= null) {
            view?.findViewById<TextView>(R.id.itcatnew_categoryName_txt)?.text = category.name
            view?.findViewById<ImageButton>(R.id.itcatnew_edit_btn)?.visibility = View.GONE
            view?.findViewById<ImageView>(R.id.itcatnew_categoryIcon_img)?.setImageResource(category.imgReference.resourceId)
        }
        return view ?: throw IllegalArgumentException("View cannot be null")
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }
}