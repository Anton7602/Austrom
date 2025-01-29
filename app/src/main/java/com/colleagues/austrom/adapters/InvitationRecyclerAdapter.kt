package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Invitation

class InvitationRecyclerAdapter (private val invitations: List<Invitation>) : RecyclerView.Adapter<InvitationRecyclerAdapter.InvitationViewHolder>() {

    class InvitationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTextView: TextView = itemView.findViewById(R.id.invitit_email_txt)
        val inviteCodeTextView: TextView = itemView.findViewById(R.id.invitit_inviteCode_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        return InvitationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_invitation, parent, false) )
    }

    override fun getItemCount(): Int {
        return invitations.size
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        val invitation = invitations[position]
        holder.emailTextView.text = invitation.userId
        holder.inviteCodeTextView.text = invitation.invitationCode
    }
}