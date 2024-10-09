package com.colleagues.austrom.dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryIconRecyclerAdapter
import com.colleagues.austrom.adapters.CategoryRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.fragments.SharedBudgetFragment
import com.colleagues.austrom.models.Category
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CategoryCreationDialogFragment : BottomSheetDialogFragment() {
    private lateinit var dialogHolder: CardView
    private lateinit var categoryNameLabel: TextView
    private lateinit var categoryNameLayout: TextInputLayout
    private lateinit var categoryNameField: TextInputEditText
    private lateinit var categoryNameCallToAction: TextView
    private lateinit var iconHolder: RecyclerView
    private lateinit var submitButton: Button
    private var currentStageId = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_category_creation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerView()
        setStage(0)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)

        submitButton.setOnClickListener {
            if (currentStageId==0) {
                currentStageId=1
                setStage(1)
            } else {

            }
        }
    }

    private fun setStage(stageId: Int) {
        categoryNameLabel.text = categoryNameField.text
        categoryNameLayout.visibility= if (stageId==0) View.VISIBLE else View.GONE
        categoryNameCallToAction.visibility= if (stageId==0) View.VISIBLE else View.GONE
        submitButton.text = if (stageId==0) "Continue" else "Create"
        categoryNameLabel.visibility = if (stageId==1) View.VISIBLE else View.GONE
        iconHolder.visibility = if (stageId==1) View.VISIBLE else View.GONE
    }

    private fun setUpRecyclerView() {
        iconHolder.adapter = CategoryIconRecyclerAdapter(iconList)
        iconHolder.layoutManager = GridLayoutManager(activity, 4, LinearLayoutManager.HORIZONTAL, false)
    }


    private fun bindViews(view: View) {
        dialogHolder = view.findViewById(R.id.catcrdial_holder_crd)
        categoryNameLabel = view.findViewById(R.id.catcrdial_categoryNameLabel_txt)
        categoryNameField = view.findViewById(R.id.catcrdial_categoryName_txt)
        categoryNameLayout = view.findViewById(R.id.catcrdial_categoryName_til)
        categoryNameCallToAction = view.findViewById(R.id.catcrdial_categoryNameCTA_txt)
        iconHolder = view.findViewById(R.id.catcrdial_iconHolder_rcv)
        submitButton = view.findViewById(R.id.catcrdial_continue_btn)
    }

    companion object {
        private val iconList: List<Int> = listOf(
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_food_temp,
            R.drawable.ic_category_rent_temp)
    }
}