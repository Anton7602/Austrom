package com.colleagues.austrom

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.TransactionGroupRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.DeletionConfirmationDialogFragment
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Transaction

class AssetPropertiesActivity : AppCompatActivity(), IDialogInitiator {
    private lateinit var asset: Asset
    private lateinit var backButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private lateinit var assetName: TextView
    private lateinit var assetOwner: TextView
    private lateinit var assetBalance: TextView
    private lateinit var assetCurrency: TextView
    private lateinit var assetPrimary: CheckBox
    private lateinit var assetPrivate: CheckBox
    private lateinit var transactionHolder: RecyclerView
    private lateinit var noTransactionsText: TextView
    private lateinit var assetCard: CardView
    private lateinit var dbProvider: IDatabaseProvider
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

        assetPrimary.setOnClickListener {
            AustromApplication.appUser?.primaryPaymentMethod = if (assetPrimary.isChecked) {asset.assetId} else {null}
            dbProvider.updateUser(AustromApplication.appUser!!)
        }

        assetPrivate.setOnClickListener {
            asset.isPrivate = assetPrivate.isChecked
            AustromApplication.activeAssets[asset.assetId]?.isPrivate = asset.isPrivate
            dbProvider.updateAsset(asset)
        }

        deleteButton.setOnClickListener {
            if (transactionsOfAsset.isEmpty()) {
                dbProvider.deleteAsset(asset)
                this.finish()
            } else {
                DeletionConfirmationDialogFragment(this).show(supportFragmentManager, "AssetDeletion Dialog")
            }
        }

        backButton.setOnClickListener {
            this.finish()
        }
    }

    private fun retrieveAssetFromIntent() {
        if (AustromApplication.selectedAsset!=null) {
            asset = AustromApplication.selectedAsset!!
        } else {
            finish()
        }
    }

    private fun setUpAssetProperties() {
        assetCard.setBackgroundResource(R.drawable.sh_card_background)
        assetName.text = asset.assetName
        assetOwner.text = AustromApplication.knownUsers[asset.userId]?.username!!.startWithUppercase()
        assetBalance.text = asset.amount.toMoneyFormat()
        assetCurrency.text = AustromApplication.activeCurrencies[asset.currencyCode]?.symbol
        assetPrimary.isChecked = (asset.assetId == AustromApplication.appUser?.primaryPaymentMethod)
        assetPrivate.isChecked = asset.isPrivate
        assetPrivate.isEnabled = AustromApplication.appUser?.userId == asset.userId
        deleteButton.isEnabled = AustromApplication.appUser?.userId == asset.userId
        if (!deleteButton.isEnabled) deleteButton.setColorFilter(R.color.dark_grey)
    }

    private fun setUpRecyclerView() {
        transactionsOfAsset = dbProvider.getTransactionsOfAsset(asset)
        noTransactionsText.visibility = if (transactionsOfAsset.isEmpty()) {View.VISIBLE} else {View.GONE}
        transactionHolder.layoutManager = LinearLayoutManager(this)
        val groupedTransactions = Transaction.groupTransactionsByDate(transactionsOfAsset)
        transactionHolder.adapter = TransactionGroupRecyclerAdapter(groupedTransactions, this)
    }

    private fun adjustInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.asdet_mainHolder_cly)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun bindViews() {
        backButton = findViewById(R.id.asdet_back_btn)
        deleteButton = findViewById(R.id.asdet_remove_btn)
        assetName = findViewById(R.id.asdet_assetName_txt)
        assetOwner = findViewById(R.id.asdet_owner_txt)
        assetBalance = findViewById(R.id.asdet_balance_txt)
        assetCurrency = findViewById(R.id.asdet_currency_txt)
        assetPrimary = findViewById(R.id.asdet_isPrimary_chb)
        assetPrivate = findViewById(R.id.asdet_isPrivate_chb)
        transactionHolder = findViewById(R.id.asdet_transactionHolder_rcv)
        noTransactionsText = findViewById(R.id.asdet_noTransactions_txt)
        assetCard = findViewById(R.id.asdet_assetCard_crd)
        dbProvider = FirebaseDatabaseProvider(this)
    }

    override fun receiveValue(value: String, valueType: String) {
        if (valueType=="DialogResult" && value=="true") {
            asset.delete(FirebaseDatabaseProvider(this))
            this.finish()
        }
    }
}