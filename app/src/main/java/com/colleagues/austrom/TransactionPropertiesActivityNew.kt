package com.colleagues.austrom

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.ImageSelectionDialogFragment
import com.colleagues.austrom.dialogs.TextEditDialogFragment
import com.colleagues.austrom.dialogs.TransactionDetailCreationNewDialogFragment
import com.colleagues.austrom.fragments.TransactionEditFragment
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.views.ActionButtonView
import com.colleagues.austrom.views.TransactionReceiptView
import java.io.File

class TransactionPropertiesActivityNew : AppCompatActivity() {
    //region Binding
    private lateinit var backButton: ImageButton
    private lateinit var moreButton: ImageButton
    private lateinit var transactionHolder: TransactionReceiptView
    private lateinit var fragmentHolder: FragmentContainerView

    private lateinit var newDetailActionButton: ActionButtonView
    private lateinit var newImageActionButton: ActionButtonView
    private lateinit var commentActionButton: ActionButtonView
    private lateinit var splitActionButton: ActionButtonView
    private fun bindViews() {
        backButton = findViewById(R.id.trdet2_backButton_btn)
        moreButton = findViewById(R.id.trdet_moreButton_btn)
        transactionHolder = findViewById(R.id.trdet_TransactionReceipt_trec)
        fragmentHolder = findViewById(R.id.trdet_editFragment_frc)

        newDetailActionButton = findViewById(R.id.trdet_newDetail_abtn)
        newImageActionButton = findViewById(R.id.trdet_newImage_abtn)
        commentActionButton = findViewById(R.id.trdet_comment_abtn)
        splitActionButton = findViewById(R.id.trdet_split_abtn)

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
            val softKeyboardHeightInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, if (softKeyboardHeightInset==0) systemBars.bottom else softKeyboardHeightInset)
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
        setUpEditFragment()

        backButton.setOnClickListener { this.finish() }
        moreButton.setOnClickListener { showMenu(moreButton, R.menu.transaction_context_menu) }
        commentActionButton.setText(if (transaction.comment.isNullOrEmpty()) getString(R.string.add_comment) else getString(R.string.edit_comment))
        val imageFile = File(externalCacheDir, "${transaction.transactionId}.jpg")

        newImageActionButton.setText(if (imageFile.exists()) getString(R.string.show_image) else getString(R.string.add_image))


        commentActionButton.setOnClickListener { launchEditCommentDialog() }
        newImageActionButton.setOnClickListener { launchImageSelectionDialog() }
        newDetailActionButton.setOnClickListener { launchNewDetailDialog() }
    }

    private fun setUpEditFragment() {
        fragmentHolder.visibility = View.GONE
        val fragment = TransactionEditFragment(transaction)
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragment.setOnDialogResultListener { result -> fragmentHolder.visibility = View.GONE }
        fragmentTransaction.replace(R.id.trdet_editFragment_frc, fragment)
        fragmentTransaction.commit()
    }

    private fun launchImageSelectionDialog() {
        val dialog = ImageSelectionDialogFragment(transaction)
        dialog.setOnDialogResultListener { isImageSelected -> }
        dialog.show(supportFragmentManager, "ImageSelectionDialog")
    }

    private fun launchNewDetailDialog() {
        val dialog = TransactionDetailCreationNewDialogFragment(transaction, LocalDatabaseProvider(this).getTransactionDetailsOfTransaction(transaction))
        dialog.setOnDialogResultListener { _ -> transactionHolder.fillInTransaction(transaction) }
        dialog.setOnDetailChangedListener { detailName, quantity, unit, amount -> transactionHolder.updateEditedTransactionDetail(detailName, quantity, unit, amount) }

        dialog.show(supportFragmentManager, "TransactionDetailCreation")
    }

    private fun launchEditCommentDialog() {
        val dialog = TextEditDialogFragment(transaction.comment, getString(R.string.comment))
        dialog.setOnTextChangedListener { editedText ->
            transaction.comment = editedText.ifEmpty { null }
            transactionHolder.fillInTransaction(transaction) }
        dialog.setOnDialogResultListener { editedText ->
            transaction.comment = editedText.ifEmpty { null }
            transactionHolder.fillInTransaction(transaction)
            commentActionButton.setText(if (transaction.comment.isNullOrEmpty()) getString(R.string.add_comment) else getString(R.string.edit_comment))
            transaction.update(LocalDatabaseProvider(this), FirebaseDatabaseProvider(this))
        }
        dialog.show(supportFragmentManager, "TextEditDialog")
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.transconmenu_edit -> {fragmentHolder.visibility = View.VISIBLE}
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