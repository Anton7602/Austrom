package com.colleagues.austrom

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.colleagues.austrom.adapters.CategoryIconRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.CategoryControlDialogFragment
import com.colleagues.austrom.dialogs.CategoryPullDialogFragment
import com.colleagues.austrom.dialogs.IconSelectionDialogFragment
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.managers.Icon
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.CategoryValidationType
import com.colleagues.austrom.models.InvalidCategoryException
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.textfield.TextInputEditText

class CategoryCreationActivity : AppCompatActivity(), IDialogInitiator {
    private lateinit var categoryName: TextInputEditText
    private lateinit var selectedIconImage: ImageView
    private lateinit var submitButton: Button
    private lateinit var deleteButton: ImageButton
    private lateinit var backButton: ImageButton
    private var isExpenseCategoryBeingCreated = false;
    private var category: Category? = null
    private var selectedIcon: Icon = Icon.I0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category_creation)
        adjustInsets()
        bindViews()

        if (intent.getStringExtra("CategoryId")!=null) {
            val categoryId = intent.getStringExtra("CategoryId")
            category = AustromApplication.activeIncomeCategories[categoryId] ?: AustromApplication.activeExpenseCategories[categoryId]
            if (category!=null) {
                categoryName.setText(category!!.name)
                selectedIconImage.setImageResource(category!!.imgReference.resourceId)
                selectedIcon = category!!.imgReference
                deleteButton.setImageResource(R.drawable.ic_navigation_delete_temp)
                submitButton.text = getString(R.string.accept)
            }
        } else {
            if (intent.getBooleanExtra("isExpenseCategory", false)){
                isExpenseCategoryBeingCreated = true
            }
            submitButton.text = getString(R.string.add_new_category)
            //(iconHolder.adapter as CategoryIconRecyclerAdapter).selectIcon(Icon.I0)
        }

        backButton.setOnClickListener { finish() }

        selectedIconImage.setOnClickListener {
            IconSelectionDialogFragment(this, selectedIcon).show(supportFragmentManager, "Category Control Dialog")
        }

        submitButton.setOnClickListener {
            if (categoryName.text.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_name_category_not_allower), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val dbProvider = LocalDatabaseProvider(this)
            if (category==null) {
                val newCategory = Category(
                    name = categoryName.text.toString(),
                    imgReference = selectedIcon,
                    transactionType = if (isExpenseCategoryBeingCreated) TransactionType.EXPENSE else TransactionType.INCOME)
                dbProvider.writeCategory(newCategory)
                if (isExpenseCategoryBeingCreated) {
                    AustromApplication.activeExpenseCategories[newCategory.categoryId] = newCategory
                } else AustromApplication.activeIncomeCategories[newCategory.categoryId] = newCategory
            } else {
                category!!.name = categoryName.text.toString()
                category!!.imgReference = selectedIcon
                dbProvider.updateCategory(category!!)
                if (category!!.transactionType == TransactionType.INCOME) {
                    AustromApplication.activeIncomeCategories[category!!.categoryId] = category!!
                } else AustromApplication.activeIncomeCategories[category!!.categoryId] = category!!
            }
            finish()
        }

        deleteButton.setOnClickListener {
            val dbProvider = LocalDatabaseProvider(this)
            if (category!=null) {
                if (dbProvider.getCategories(category!!.transactionType).size<=1) {
                    Toast.makeText(this, getString(R.string.category_delete_not_allowed), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val transactions = dbProvider.getTransactionOfCategory(category!!)
                if (transactions.isEmpty()) {
                    dbProvider.deleteCategory(category!!)
                    when (category!!.transactionType) {
                        TransactionType.INCOME -> AustromApplication.activeIncomeCategories.remove(category!!.categoryId)
                        TransactionType.EXPENSE -> AustromApplication.activeExpenseCategories.remove(category!!.categoryId)
                        TransactionType.TRANSFER -> {}
                    }
                    finish()
                } else {
                    CategoryPullDialogFragment(category!!, transactions, this).show(supportFragmentManager, "Transaction Target Dialog")
                }
            }
        }

        AustromApplication.showKeyboard(this, categoryName)
    }

    private fun bindViews() {
        selectedIconImage = findViewById(R.id.catcr_categoryIcon_img)
        categoryName = findViewById(R.id.catcr_categoryName_txt)
        submitButton = findViewById(R.id.catcr_acceptButton_btn)
        deleteButton = findViewById(R.id.catcr_negativeResult_btn)
        backButton = findViewById(R.id.catcr_backButton_btn)
        //iconHolder = findViewById(R.id.catcr_categoryIcon_rcv)
    }

    private fun adjustInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
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
                    selectedIcon = Icon.entries.find { l -> l.resourceId == drawableID } ?: Icon.I0
                }
            }
        }
    }
}