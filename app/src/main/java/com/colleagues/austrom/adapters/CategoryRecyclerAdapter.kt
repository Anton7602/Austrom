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
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.CategoryCreationActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.managers.Icon
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.TransactionType

class CategoryRecyclerAdapter(private val categories: MutableList<Category>, private val activity: AppCompatActivity, private val isAddNewCategoryAllowed: Boolean = false, private val isEditCategoryAllowed: Boolean = false): RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryNewViewHolder>()  {
    class CategoryNewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryHolder: LinearLayout = itemView.findViewById(R.id.itcatnew_categoryHolder_lly)
        val categoryName: TextView = itemView.findViewById(R.id.itcatnew_categoryName_txt)
        val categoryImage: ImageView = itemView.findViewById(R.id.itcatnew_categoryIcon_img)
        val editButton: ImageButton = itemView.findViewById(R.id.itcatnew_edit_btn)
    }
    private var returnClickedItem: (Category)->Unit = {}
    fun setOnItemClickListener(l: ((Category)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryNewViewHolder { return CategoryNewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false))}
    override fun getItemCount(): Int { return categories.size }

    init { if (isAddNewCategoryAllowed) categories.add(0, Category(transactionType = categories[0].transactionType, name= activity.getString(R.string.add_new_category), imgReference = Icon.I64, categoryId = ""))  }

    override fun onBindViewHolder(holder: CategoryNewViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryName.text = category.name
        holder.categoryImage.setImageResource(category.imgReference.resourceId)
        holder.editButton.visibility = if (category.categoryId.isEmpty() || !isEditCategoryAllowed) View.GONE else View.VISIBLE
        if (isEditCategoryAllowed) {
            holder.categoryHolder.setOnClickListener {
                if (category.categoryId.isNotEmpty()) {
                    activity.startActivity(Intent(activity, CategoryCreationActivity::class.java).putExtra("CategoryId", categories[position].categoryId))
                } else {
                    activity.startActivity(Intent(activity, CategoryCreationActivity::class.java).putExtra("isExpenseCategory", (category.transactionType == TransactionType.EXPENSE)))
                }
            }
        } else {
            returnClickedItem(category)
        }
    }
}