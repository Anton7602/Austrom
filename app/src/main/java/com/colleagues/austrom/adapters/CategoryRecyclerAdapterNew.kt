package com.colleagues.austrom.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.CategoryCreationActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.TransactionPropertiesActivity
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Category

class CategoryRecyclerAdapterNew(private val categories: List<Category>,
                                 private val activity: AppCompatActivity,
                                 private val receiver: IDialogInitiator? = null): RecyclerView.Adapter<CategoryRecyclerAdapterNew.CategoryNewViewHolder>()  {

    class CategoryNewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryHolder: LinearLayout = itemView.findViewById(R.id.itcatnew_categoryHolder_lly)
        val categoryName: TextView = itemView.findViewById(R.id.itcatnew_categoryName_txt)
        val categoryImage: ImageView = itemView.findViewById(R.id.itcatnew_categoryIcon_img)
        val editButton: ImageButton = itemView.findViewById(R.id.itcatnew_edit_btn)
        val deleteButton: ImageButton = itemView.findViewById(R.id.itcatnew_delete_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryNewViewHolder {
        return CategoryNewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_category_new, parent, false))
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryNewViewHolder, position: Int) {
        holder.categoryName.text = categories[position].name
        holder.categoryImage.setImageResource(categories[position].imgReference?.resourceId ?: R.drawable.ic_placeholder_icon)
        holder.categoryHolder.setOnClickListener {
            activity.startActivity(Intent(activity, CategoryCreationActivity::class.java).putExtra("CategoryId", categories[position].id))
        }
    }
}