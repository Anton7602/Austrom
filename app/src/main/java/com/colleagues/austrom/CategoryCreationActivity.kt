package com.colleagues.austrom

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.CategoryIconRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.CategoryPullDialogFragment
import com.colleagues.austrom.dialogs.DeletionConfirmationDialogFragment
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.managers.IconManager
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.textfield.TextInputEditText

class CategoryCreationActivity : AppCompatActivity(), IDialogInitiator {
    private lateinit var categoryName: TextInputEditText
    private lateinit var selectedIconImage: ImageView
    private lateinit var iconHolder: RecyclerView
    private lateinit var submitButton: ImageButton
    private lateinit var declineButton: ImageButton
    private var isExpenseCategoryBeingCreated = false;
    private var category: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category_creation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindViews()

        iconHolder.adapter = CategoryIconRecyclerAdapter(IconManager().getAllAvailableIcons(), this)
        iconHolder.layoutManager = GridLayoutManager(this, 5, LinearLayoutManager.VERTICAL, false)

        if (intent.getStringExtra("CategoryId")!=null) {
            val dbProvider = LocalDatabaseProvider(this)
            category = dbProvider.getCategoryById(intent.getStringExtra("CategoryId")!!)
            if (category!=null) {
                categoryName.setText(category!!.name)
                if (category!!.imgReference!=null) {
                    selectedIconImage.setImageResource(category!!.imgReference!!.resourceId)
                    (iconHolder.adapter as CategoryIconRecyclerAdapter).selectedIcon = category!!.imgReference
                }
                declineButton.setImageResource(R.drawable.ic_navigation_delete_temp)
            }
        } else {
            if (intent.getBooleanExtra("isExpenseCategory", false)){
                isExpenseCategoryBeingCreated = true
            }
        }

        submitButton.setOnClickListener {
            if (categoryName.text.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_name_category_not_allower), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val dbProvider = LocalDatabaseProvider(this)
            if (category==null) {
                dbProvider.writeCategory(Category(
                    id = Category.generateCategoryId(),
                    name = categoryName.text.toString(),
                    type = "Mandatory",
                    imgReference = (iconHolder.adapter as CategoryIconRecyclerAdapter).selectedIcon,
                    transactionType = if (isExpenseCategoryBeingCreated) TransactionType.EXPENSE else TransactionType.INCOME))
            } else {
                category!!.name = categoryName.text.toString()
                category!!.imgReference = (iconHolder.adapter as CategoryIconRecyclerAdapter).selectedIcon
                dbProvider.updateCategory(category!!)
            }
            finish()
        }

        declineButton.setOnClickListener {
            if (categoryName.text.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_name_category_not_allower), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val dbProvider = LocalDatabaseProvider(this)
            if (category==null) {
                finish()
            } else {
                if (dbProvider.getCategories(category!!.transactionType).size<=1) {
                    Toast.makeText(this, getString(R.string.category_delete_not_allowed), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val transactions = dbProvider.getTransactionOfCategory(category!!)
                if (transactions.isEmpty()) {
                    dbProvider.deleteCategory(category!!)
                    finish()
                } else {
                    CategoryPullDialogFragment(category!!, transactions, this).show(supportFragmentManager, "Transaction Target Dialog")
                }
            }
        }

        iconHolder.setOnClickListener {
            if ((iconHolder.adapter as CategoryIconRecyclerAdapter).selectedIcon?.resourceId != null) {
                selectedIconImage.setImageResource((iconHolder.adapter as CategoryIconRecyclerAdapter).selectedIcon!!.resourceId)
            }
        }

        AustromApplication.showKeyboard(this, categoryName)
    }

    private fun bindViews() {
        categoryName = findViewById(R.id.catcr_categoryName_txt)
        selectedIconImage = findViewById(R.id.catcr_categoryIcon_img)
        submitButton = findViewById(R.id.catcr_positiveResult_btn)
        declineButton = findViewById(R.id.catcr_negativeResult_btn)
        iconHolder = findViewById(R.id.catcr_categoryIcon_rcv)
    }

    override fun receiveValue(value: String, valueType: String) {
        when(valueType) {
            "TransactionTransfer" -> {
                if (value == "Success") {
                    if (category!=null) {
                        val dbProvider = LocalDatabaseProvider(this)
                        dbProvider.deleteCategory(category!!)
                        finish()
                    }
                }
            }
            "SelectedIcon" -> {
                val drawableID = value.toIntOrNull()
                if (drawableID!=null) {
                    selectedIconImage.setImageResource(drawableID)
                }
            }
        }
    }
}