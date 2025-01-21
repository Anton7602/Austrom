package com.colleagues.austrom

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.views.TransactionReceiptView

class TransactionPropertiesActivityNew : AppCompatActivity() {
    //region Binding
    private lateinit var backButton: ImageButton
    private lateinit var moreButton: ImageButton
    private lateinit var transactionHolder: TransactionReceiptView
    private fun bindViews() {
        backButton = findViewById(R.id.trdet2_backButton_btn)
        moreButton = findViewById(R.id.trdet_moreButton_btn)
        transactionHolder = findViewById(R.id.trdet_TransactionReceipt_trec)
    }
    //endregion
    //region Localization
    override fun attachBaseContext(newBase: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.attachBaseContext(newBase)
        } else  {
            super.attachBaseContext(AustromApplication.updateBaseContextLocale(newBase))
        }
    }
    //endregion
    //region Styling
    private fun adjustInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }
    //endregion
    private lateinit var transaction: Transaction


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_properties_new)
        adjustInsets()
        bindViews()

        retrieveTransactionFromIntent()
        transactionHolder.fillInTransaction(transaction)

        backButton.setOnClickListener { this.finish() }
        moreButton.setOnClickListener { showMenu(moreButton, R.menu.transaction_context_menu) }

    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.transconmenu_edit -> {}  //TODO("Edit Section")
                R.id.transconmenu_delete -> { transaction.cancel(LocalDatabaseProvider(this), FirebaseDatabaseProvider(this)); this.finish() }
            }
            true
        }
        popup.setOnDismissListener { }
        popup.show()
    }

    private fun retrieveTransactionFromIntent() {
        val dbProvider = LocalDatabaseProvider(this)
        val transactionId = intent.getStringExtra("transactionId")
        if (transactionId!=null) {
            val receivedTransaction =  dbProvider.getTransactionByID(transactionId)
            if (receivedTransaction!=null) transaction=receivedTransaction else finish()
        }
    }
}