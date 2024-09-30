package com.colleagues.austrom

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TransactionPropertiesActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var sourceText: TextView
    private lateinit var targetText: TextView
    private lateinit var amount: TextView
    private lateinit var revokeTransactionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_properties)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindViews()

        backButton.setOnClickListener {
            this.finish()
        }

        revokeTransactionButton.setOnClickListener {

        }
    }

    private fun bindViews() {
        backButton = findViewById(R.id.trdet_back_btn)
        sourceText = findViewById(R.id.trdet_source_txt)
        targetText = findViewById(R.id.trdet_target_txt)
        amount = findViewById(R.id.trdet_amount_txt)
        revokeTransactionButton = findViewById(R.id.trdet_revoke_btn)
    }
}