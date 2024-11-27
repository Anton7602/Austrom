package com.colleagues.austrom

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.CategoryIconRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.managers.IconManager
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.textfield.TextInputEditText

class CategoryCreationActivity : AppCompatActivity(), IDialogInitiator {
    private lateinit var categoryName: TextInputEditText
    private lateinit var selectedIconImage: ImageView
    private lateinit var iconHolder: RecyclerView
    private lateinit var submitButton: Button
    private lateinit var declineButton: Button
    private var category: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category_creation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            //v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        bindViews()

        iconHolder.adapter = CategoryIconRecyclerAdapter(IconManager().getAllAvailableIcons(), this)
        iconHolder.layoutManager = GridLayoutManager(this, 5, LinearLayoutManager.VERTICAL, false)

        if (intent.getStringExtra("CategoryId")!=null) {
            val dbProvider = LocalDatabaseProvider(this)
            category = dbProvider.getCategoryById(intent.getStringExtra("CategoryId")!!)
            if (category!=null) {
                submitButton.text = getText(R.string.update)
                declineButton.text = getText(R.string.delete)
                submitButton.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_navigation_up_temp,0)
                declineButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_delete_temp,0,0,0)
                categoryName.setText(category!!.name)
                if (category!!.imgReference!=null) {
                    selectedIconImage.setImageResource(category!!.imgReference!!.resourceId)
                    (iconHolder.adapter as CategoryIconRecyclerAdapter).selectedIcon = category!!.imgReference
                }
            }

        }

        submitButton.setOnClickListener {
            val dbProvider = LocalDatabaseProvider(this)
            if (category==null) {
                dbProvider.writeCategory(Category(
                    id = Category.generateCategoryId(),
                    name = categoryName.text.toString(),
                    type = "Mandatory",
                    imgReference = (iconHolder.adapter as CategoryIconRecyclerAdapter).selectedIcon,
                    transactionType = TransactionType.INCOME))
            } else {
                category!!.name = categoryName.text.toString()
                category!!.imgReference = (iconHolder.adapter as CategoryIconRecyclerAdapter).selectedIcon
                dbProvider.updateCategory(category!!)
            }
            finish()
        }

        declineButton.setOnClickListener {
            val dbProvider = LocalDatabaseProvider(this)
            if (category==null) {
                dbProvider.writeCategory(Category(
                    id = Category.generateCategoryId(),
                    name = categoryName.text.toString(),
                    type = "Mandatory",
                    imgReference = (iconHolder.adapter as CategoryIconRecyclerAdapter).selectedIcon,
                    transactionType = TransactionType.EXPENSE))
            } else {
                var transactions = dbProvider.getTransactionOfCategory(category!!)
                transactions.forEach{ transaction ->
                    transaction.categoryId = category!!.id
                    //TODO("FINISH!!!!")
                }
            }
            finish()
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
        val drawableID = value.toIntOrNull()
        if (drawableID!=null) {
            selectedIconImage.setImageResource(drawableID)
        }
    }
}