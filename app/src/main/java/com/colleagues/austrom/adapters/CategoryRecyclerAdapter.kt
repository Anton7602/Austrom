package com.colleagues.austrom.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.dialogs.CategoryCreationDialogFragment
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Category

class CategoryRecyclerAdapter(private val categories: MutableList<Category>,
                              private val activity: AppCompatActivity, private val receiver: IDialogInitiator? = null,
                              private val isShowingCreateNewCategoryButton: Boolean = false): RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryViewHolder>() {
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryHolder: CardView = itemView.findViewById(R.id.itcat_holder_crd)
        val categoryName: TextView = itemView.findViewById(R.id.itcat_categoryName_txt)
        val categoryImage: ImageView = itemView.findViewById(R.id.itcat_categoruIcon_img)
        var isSelected = false
    }

    init {
        if (isShowingCreateNewCategoryButton) categories.add(Category("New Category","Add New Category"))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false))
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        if (isShowingCreateNewCategoryButton && position==itemCount-1) {
            holder.categoryName.text = activity.getString(R.string.add_new_category)
            holder.categoryName.setTextColor(Color.GRAY)
            holder.isSelected = false
            holder.categoryImage.setImageResource(R.drawable.ic_navigation_add_temp)
            holder.categoryImage.setColorFilter(Color.GRAY)
            holder.categoryHolder.setBackgroundResource(R.drawable.sh_category_add_background)
            holder.categoryHolder.elevation=0f
            holder.categoryHolder.setOnClickListener {
                CategoryCreationDialogFragment(categories[position-1].transactionType, receiver).show(activity.supportFragmentManager, "Category Creation Dialog")
            }
        } else {
            holder.categoryName.text = categories[position].name
            //holder.isSelected = AustromApplication.appUser?.categories?.find { entry -> entry.name == categories[position].name } != null
            holder.categoryImage.setImageResource(categories[position].imgReference?.resourceId ?: R.drawable.ic_placeholder_icon)
            holder.categoryHolder.setBackgroundResource(if (holder.isSelected) R.drawable.sh_card_background else R.drawable.sh_category_deselected)
            //holder.categoryHolder.setBackgroundResource(R.drawable.sh_card_background)
            holder.categoryHolder.setOnClickListener {
                if (holder.isSelected) {
                    holder.isSelected = false
                    holder.categoryHolder.elevation = 1f
                    holder.categoryHolder.setBackgroundResource(R.drawable.sh_category_deselected)
                    //AustromApplication.appUser?.categories?.remove(categories[position])
                    if (AustromApplication.knownUsers.isNotEmpty()) {
                        //AustromApplication.knownUsers[AustromApplication.appUser!!.userId]?.categories?.remove(categories[position])
                    }
                } else {
                    holder.isSelected = true
                    holder.categoryHolder.elevation = 4f
                    holder.categoryHolder.setBackgroundResource(R.drawable.sh_card_background)
                    //AustromApplication.appUser?.categories?.add(categories[position])
                    //AustromApplication.knownUsers[AustromApplication.appUser!!.userId]?.categories?.add(categories[position])
                }
            }
        }
    }
}