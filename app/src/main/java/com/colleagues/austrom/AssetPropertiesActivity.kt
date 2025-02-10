package com.colleagues.austrom

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.TransactionGroupRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.DeletionConfirmationDialogFragment
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.fragments.AssetEditFragment
import com.colleagues.austrom.fragments.TransactionEditFragment
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Transaction

class AssetPropertiesActivity : AppCompatActivity(){
    //region Binding
    private lateinit var backButton: ImageButton
    private lateinit var moreButton: ImageButton
    private lateinit var assetName: TextView
    private lateinit var assetOwner: TextView
    private lateinit var assetBalance: TextView
    private lateinit var assetCurrency: TextView
    private lateinit var assetPrimary: CheckBox
    private lateinit var assetPrivate: CheckBox
    private lateinit var transactionHolder: RecyclerView
    private lateinit var noTransactionsText: TextView
    private lateinit var assetCard: CardView
    private lateinit var dbProvider: LocalDatabaseProvider
    private lateinit var fragmentHolder: FragmentContainerView
    private fun bindViews() {
        backButton = findViewById(R.id.asdet_back_btn)
        moreButton = findViewById(R.id.asdet_remove_btn)
        assetName = findViewById(R.id.asdet_assetName_txt)
        assetOwner = findViewById(R.id.asdet_owner_txt)
        assetBalance = findViewById(R.id.asdet_balance_txt)
        assetCurrency = findViewById(R.id.asdet_currency_txt)
        assetPrimary = findViewById(R.id.asdet_isPrimary_chb)
        assetPrivate = findViewById(R.id.asdet_isPrivate_chb)
        transactionHolder = findViewById(R.id.asdet_transactionHolder_rcv)
        noTransactionsText = findViewById(R.id.asdet_noTransactions_txt)
        assetCard = findViewById(R.id.asdet_assetCard_crd)
        dbProvider = LocalDatabaseProvider(this)
        fragmentHolder = findViewById(R.id.asdet_fragmentHolder_frh)
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.asdet_mainHolder_cly)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }
    //endregion
    private lateinit var asset: Asset
    private var transactionsOfAsset: MutableList<Transaction> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_asset_properties)
        adjustInsets()
        bindViews()
        retrieveAssetFromIntent()
        setUpAssetProperties()
        setUpRecyclerView()
        setUpEditFragment()

        assetPrimary.setOnClickListener { markAssetAsPrimary() }
        assetPrivate.setOnClickListener { markAssetAsPrivate() }
        moreButton.setOnClickListener { showMenu(moreButton, R.menu.asset_context_menu) }
        backButton.setOnClickListener { this.finish() }
    }

    private fun setUpEditFragment() {
        fragmentHolder.visibility = View.GONE
        val fragment = AssetEditFragment(asset)
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragment.setOnDialogResultListener { asset ->
            val remoteDBProvider = FirebaseDatabaseProvider(this)
            fragmentHolder.visibility = View.GONE
            asset.update(dbProvider, remoteDBProvider)
            setUpAssetProperties()
        }
        fragmentTransaction.replace(R.id.asdet_fragmentHolder_frh, fragment)
        fragmentTransaction.commit()
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.asconmenu_delete -> { tryDeleteAsset()}
                R.id.asconmenu_edit -> { fragmentHolder.visibility = View.VISIBLE }
            }
            true
        }
        popup.setOnDismissListener { }
        popup.show()
    }

    private fun retrieveAssetFromIntent() {
        val assetId = intent.getStringExtra("assetId")
        if (AustromApplication.activeAssets[assetId]!=null) asset=AustromApplication.activeAssets[assetId]!! else finish()
    }

    private fun tryDeleteAsset() {
        if (transactionsOfAsset.isEmpty()) {
            asset.delete(LocalDatabaseProvider(this), FirebaseDatabaseProvider(this))
            this.finish()
        } else {
            val dialog = DeletionConfirmationDialogFragment()
            dialog.setOnDialogResultListener { isDeletionConfirmed ->  if (isDeletionConfirmed) { asset.delete(LocalDatabaseProvider(this), FirebaseDatabaseProvider(this)); this.finish() }}
            dialog.show(supportFragmentManager, "AssetDeletion Dialog")
        }
    }

    private fun markAssetAsPrimary() {
        AustromApplication.appUser?.primaryPaymentMethod = if (assetPrimary.isChecked) {asset.assetId} else {null}
        dbProvider.updateUser(AustromApplication.appUser!!)
    }

    private fun markAssetAsPrivate() {
        asset.isPrivate = assetPrivate.isChecked
        AustromApplication.activeAssets[asset.assetId]?.isPrivate = asset.isPrivate
        dbProvider.updateAsset(asset)
    }

    private fun setUpAssetProperties() {
        assetCard.setBackgroundResource(R.drawable.sh_card_background)
        assetName.text = asset.assetName
        assetOwner.text = if (AustromApplication.appUser!!.activeBudgetId!=null) {
            AustromApplication.knownUsers[asset.userId]?.username!!.startWithUppercase()
        } else {
            AustromApplication.appUser?.username ?: "Unknown"
        }
        assetBalance.text = asset.amount.toMoneyFormat()
        assetCurrency.text = AustromApplication.activeCurrencies[asset.currencyCode]?.symbol
        assetPrimary.isChecked = (asset.assetId == AustromApplication.appUser?.primaryPaymentMethod)
        assetPrivate.isChecked = asset.isPrivate
        assetPrivate.isEnabled = AustromApplication.appUser?.userId == asset.userId
        moreButton.isEnabled = AustromApplication.appUser?.userId == asset.userId
        if (!moreButton.isEnabled) moreButton.setColorFilter(R.color.dark_grey)
    }

    private fun setUpRecyclerView() {
        transactionsOfAsset = dbProvider.getTransactionsOfAsset(asset)
        noTransactionsText.visibility = if (transactionsOfAsset.isEmpty()) {View.VISIBLE} else {View.GONE}
        transactionHolder.layoutManager = LinearLayoutManager(this)
        val groupedTransactions = Transaction.groupTransactionsByDate(transactionsOfAsset)
        val adapter = TransactionGroupRecyclerAdapter(groupedTransactions, this)
        adapter.setOnItemClickListener { transaction, _ -> startActivity(Intent(this, TransactionPropertiesActivityNew::class.java).putExtra("transactionId", transaction.transactionId)) }
        transactionHolder.adapter = adapter
    }
}