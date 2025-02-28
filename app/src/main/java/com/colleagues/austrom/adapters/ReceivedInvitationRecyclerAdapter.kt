package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Invitation

class ReceivedInvitationRecyclerAdapter(private val invitingBudgets: List<Budget>) : RecyclerView.Adapter<ReceivedInvitationRecyclerAdapter.InvitationViewHolder>() {
    private var returnAcceptedItem: (Budget)->Unit = {}
    fun setOnItemAcceptClickListener(l: ((Budget)->Unit)) { returnAcceptedItem = l }
    private var returnDeclinedItem: (Budget)->Unit = {}
    fun setOnItemDeclineClickListener(l: ((Budget)->Unit)) { returnDeclinedItem = l }
    class InvitationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.recinvitit_holder_crd)
        val budgetNameTextView: TextView = itemView.findViewById(R.id.recinvitit_budgetName_txt)
        val acceptButton: ImageButton = itemView.findViewById(R.id.recinvitit_accept_btn)
        val declineButton: ImageButton = itemView.findViewById(R.id.recinvitit_decline_btn)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder { return InvitationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_received_invitation, parent, false)) }
    override fun getItemCount(): Int { return invitingBudgets.size }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        val budget = invitingBudgets[position]
        holder.budgetNameTextView.text = budget.budgetName
        holder.acceptButton.setOnSafeClickListener { returnAcceptedItem(budget) }
        holder.declineButton.setOnSafeClickListener { returnDeclinedItem(budget) }
    }
}