package com.colleagues.austrom

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.CategoryIconRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.managers.IconManager
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.textfield.TextInputEditText

class CategoryCreationActivity : AppCompatActivity() {
    private lateinit var categoryName: TextInputEditText
    private lateinit var iconHolder: RecyclerView
    private lateinit var submitButton: Button

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

        iconHolder.adapter = CategoryIconRecyclerAdapter(IconManager().getAllAvailableIcons())
        iconHolder.layoutManager = GridLayoutManager(this, 5, LinearLayoutManager.VERTICAL, false)

        if (intent.getStringExtra("CategoryName")!=null) {
            submitButton.text = getText(R.string.accept)
            categoryName.setText(intent.getStringExtra("CategoryName"))
        }

        submitButton.setOnClickListener {
            val dbProvider = LocalDatabaseProvider(this)
            dbProvider.writeCategory(Category(
                name = categoryName.text.toString(),
                type = "Mandatory",
                imgReference = (iconHolder.adapter as CategoryIconRecyclerAdapter).selectedIcon,
                transactionType = TransactionType.EXPENSE))
            finish()
        }
    }

    private fun bindViews() {
        categoryName = findViewById(R.id.catcr_categoryName_txt)
        submitButton = findViewById(R.id.catcr_submit_btn)
        iconHolder = findViewById(R.id.catcr_categoryIcon_rcv)
    }
}