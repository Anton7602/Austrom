package com.colleagues.austrom

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.TransactionGroupRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Transaction

class AssetPropertiesActivity : AppCompatActivity() {
    private lateinit var asset: Asset
    private lateinit var backButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private lateinit var assetName: TextView
    private lateinit var assetOwner: TextView
    private lateinit var assetBalance: TextView
    private lateinit var assetCurrency: TextView
    private lateinit var assetPrimary: CheckBox
    private lateinit var transactionHolder: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_asset_properties)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindViews()

        if (intent.getStringExtra("Asset")!=null) {
            asset = Asset.parseFromString(intent.getStringExtra("Asset")!!)
        } else {
            finish()
        }

        assetName.text = asset.assetName
        val username = AustromApplication.knownUsers[asset.userId]?.username
        if (username!=null) {
            assetOwner.text = username.first().uppercaseChar()+username.substring(1)
        }
        assetBalance.text = String.format("%.2f", asset.amount)
        assetCurrency.text = AustromApplication.activeCurrencies[asset.currencyCode]?.symbol
        assetPrimary.isChecked = asset.isPrimary

        setUpRecyclerView()

        assetPrimary.setOnClickListener {
            val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(this)
            if (assetPrimary.isChecked) {
                val oldPrimaryAsset = AustromApplication.activeAssets.filter { entry -> entry.value.isPrimary }
                for (oldAsset in oldPrimaryAsset) {
                    oldAsset.value.isPrimary = false
                    dbProvider.updateAsset(oldAsset.value)
                }
                asset.isPrimary = true
                AustromApplication.activeAssets[asset.assetId]?.isPrimary = true
                dbProvider.updateAsset(asset)
            } else {
                asset.isPrimary = false
                AustromApplication.activeAssets[asset.assetId]?.isPrimary = false
                dbProvider.updateAsset(asset)
            }
        }

        backButton.setOnClickListener {
            this.finish()
        }
    }

    private fun setUpRecyclerView() {
        val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(this)
        transactionHolder.layoutManager = LinearLayoutManager(this)
        val groupedTransactions = Transaction.groupTransactionsByDate(dbProvider.getTransactionsOfAsset(asset))
        transactionHolder.adapter = TransactionGroupRecyclerAdapter(groupedTransactions, this)
    }

    private fun bindViews() {
        backButton = findViewById(R.id.asdet_back_btn)
        deleteButton = findViewById(R.id.asdet_remove_btn)
        assetName = findViewById(R.id.asdet_assetName_txt)
        assetOwner = findViewById(R.id.asdet_owner_txt)
        assetBalance = findViewById(R.id.asdet_balance_txt)
        assetCurrency = findViewById(R.id.asdet_currency_txt)
        assetPrimary = findViewById(R.id.asdet_isPrimary_chb)
        transactionHolder = findViewById(R.id.asdet_transactionHolder_rcv)
    }
}