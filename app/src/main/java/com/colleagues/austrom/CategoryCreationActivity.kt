package com.colleagues.austrom

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.TransactionGroupRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.CategoryPullDialogFragment
import com.colleagues.austrom.dialogs.bottomsheetdialogs.IconSelectionDialogFragment
import com.colleagues.austrom.managers.Icon
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.textfield.TextInputEditText

class CategoryCreationActivity : AppCompatActivity() {
    //region Binding
    private lateinit var categoryName: TextInputEditText
    private lateinit var selectedIconImage: ImageView
    private lateinit var submitButton: Button
    private lateinit var moreButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var transactionHolder: RecyclerView
    private fun bindViews() {
        selectedIconImage = findViewById(R.id.catcr_categoryIcon_img)
        categoryName = findViewById(R.id.catcr_categoryName_txt)
        submitButton = findViewById(R.id.catcr_acceptButton_btn)
        moreButton = findViewById(R.id.catcr_negativeResult_btn)
        backButton = findViewById(R.id.catcr_backButton_btn)
        transactionHolder = findViewById(R.id.catcr_transactionHolder_rcv)
        //iconHolder = findViewById(R.id.catcr_categoryIcon_rcv)
    }
    //endregion
    //region Styling
    private fun adjustInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val softKeyboardHeightInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, if (softKeyboardHeightInset==0) systemBars.bottom else softKeyboardHeightInset)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setUpOrientationLimitations() { setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) }
    // endregion
    private var isExpenseCategoryBeingCreated = false
    private var transactionsOfCategory = mutableListOf<Transaction>()
    private var category: Category? = null
    private var selectedIcon: Icon = Icon.I0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setUpOrientationLimitations()
        setContentView(R.layout.activity_category_creation)
        adjustInsets()
        bindViews()
        getCategoryFromIntent()
        setUpRecyclerView()
        backButton.setOnClickListener { finish() }
        selectedIconImage.setOnClickListener { launchIconSelectionDialog() }
        submitButton.setOnClickListener { commitCategory() }
        moreButton.setOnClickListener { showMenu(moreButton, R.menu.category_context_menu) }
        AustromApplication.showKeyboard(this, categoryName)
    }

    private fun setUpRecyclerView() {
        if (category!=null) {
            val dbProvider = LocalDatabaseProvider(this)
            transactionsOfCategory = dbProvider.getTransactionOfCategory(category!!)
            //noTransactionsText.visibility = if (transactionsOfAsset.isEmpty()) {View.VISIBLE} else {View.GONE}
            transactionHolder.layoutManager = LinearLayoutManager(this)
            val groupedTransactions = Transaction.groupTransactionsByDate(transactionsOfCategory)
            val adapter = TransactionGroupRecyclerAdapter(groupedTransactions, this)
            adapter.setOnItemClickListener { transaction, _ -> startActivity(Intent(this, TransactionPropertiesActivityNew::class.java).putExtra("transactionId", transaction.transactionId)) }
            transactionHolder.adapter = adapter
        }
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.catconmenu_delete -> {tryDeleteCategory()}
            }
            true
        }
        popup.setOnDismissListener { }
        popup.show()
    }

    private fun getCategoryFromIntent() {
        if (intent.getStringExtra("CategoryId")!=null) {
            val categoryId = intent.getStringExtra("CategoryId")
            category = AustromApplication.activeCategories[categoryId]
            if (category!=null) {
                categoryName.setText(category!!.name)
                selectedIconImage.setImageResource(category!!.imgReference.resourceId)
                selectedIcon = category!!.imgReference
                moreButton.setImageResource(R.drawable.ic_navigation_more_temp)
                submitButton.text = getString(R.string.accept)
            }
        } else {
            if (intent.getBooleanExtra("isExpenseCategory", false)){
                isExpenseCategoryBeingCreated = true
            }
            moreButton.visibility = View.GONE
            submitButton.text = getString(R.string.add_new_category)
        }
    }

    private fun launchIconSelectionDialog() {
        val dialog = IconSelectionDialogFragment(selectedIcon)
        dialog.setOnDialogResultListener { icon ->
            selectedIconImage.setImageResource(icon.resourceId)
            selectedIcon = icon
        }
        dialog.show(supportFragmentManager, "Category Control Dialog")
    }

    private fun commitCategory() {
        if (categoryName.text.toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_name_category_not_allowed), Toast.LENGTH_LONG).show()
            return
        }
        val dbProvider = LocalDatabaseProvider(this)
        if (category==null) {
            val newCategory = Category(
                name = categoryName.text.toString(),
                imgReference = selectedIcon,
                transactionType = if (isExpenseCategoryBeingCreated) TransactionType.EXPENSE else TransactionType.INCOME)
            dbProvider.writeCategory(newCategory)
            if (AustromApplication.appUser!!.activeBudgetId!=null) {
                val remoteDbProvider = FirebaseDatabaseProvider(this)
                val budget = remoteDbProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
                if (budget!=null)  remoteDbProvider.insertCategory(newCategory, budget)
            }
            AustromApplication.activeCategories[newCategory.categoryId] = newCategory
        } else {
            category!!.name = categoryName.text.toString()
            category!!.imgReference = selectedIcon
            dbProvider.updateCategory(category!!)
            if (AustromApplication.appUser!!.activeBudgetId!=null) {
                val remoteDbProvider = FirebaseDatabaseProvider(this)
                val budget = remoteDbProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
                if (budget!=null)  remoteDbProvider.updateCategory(category!!, budget)
            }
            AustromApplication.activeCategories[category!!.categoryId] = category!!
        }
        finish()
    }

    private fun tryDeleteCategory() {
        val dbProvider = LocalDatabaseProvider(this)
        if (category!=null) {
            if (dbProvider.getCategories(category!!.transactionType).size<=1) {
                Toast.makeText(this, getString(R.string.category_delete_not_allowed), Toast.LENGTH_LONG).show()
                return
            }
            val transactions = dbProvider.getTransactionOfCategory(category!!)
            if (transactions.isEmpty()) {
                dbProvider.deleteCategory(category!!)
                AustromApplication.activeCategories.remove(category!!.categoryId)
                if (AustromApplication.appUser!!.activeBudgetId!=null) {
                    val remoteDBProvider = FirebaseDatabaseProvider(this)
                    val budget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
                    if (budget!=null) {
                        remoteDBProvider.deleteCategory(category!!, budget)
                    }
                }
                finish()
            } else {
                val dialog = CategoryPullDialogFragment(category!!, transactions)
                dialog.setOnDialogResultListener { isTransactionPullingSuccessful -> if (isTransactionPullingSuccessful) {
                    dbProvider.deleteCategory(category!!)
                    if (AustromApplication.appUser!!.activeBudgetId!=null) {
                        val remoteDBProvider = FirebaseDatabaseProvider(this)
                        val budget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
                        if (budget!=null) {
                            remoteDBProvider.deleteCategory(category!!, budget)
                        }
                    }
                    AustromApplication.activeCategories.remove(category?.categoryId)
                    finish()
                }}
                dialog.show(supportFragmentManager, "Transaction Target Dialog")
            }
        }
    }
}